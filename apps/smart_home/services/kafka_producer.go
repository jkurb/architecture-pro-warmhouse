package services

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/segmentio/kafka-go"
)

// TelemetryEvent represents a telemetry event sent to Kafka.
// Fields align with the AsyncAPI TelemetryEventPayload schema.
type TelemetryEvent struct {
	EventID    string  `json:"event_id"`
	DeviceID   string  `json:"device_id"`
	HouseID    string  `json:"house_id"`
	MetricName string  `json:"metric_name"`
	Value      float64 `json:"value"`
	Unit       string  `json:"unit"`
	Timestamp  string  `json:"timestamp"`
}

// DeviceEvent represents a sensor CRUD event published to the device.events
// topic so that Device Registry can stay in sync with the monolith.
type DeviceEvent struct {
	EventType  string `json:"event_type"`
	DeviceID   int    `json:"device_id"`
	DeviceName string `json:"device_name"`
	DeviceType string `json:"device_type"`
	Location   string `json:"location"`
	Status     string `json:"status"`
	Timestamp  string `json:"timestamp"`
}

// KafkaProducer handles publishing events to Kafka.
type KafkaProducer struct {
	telemetryWriter *kafka.Writer
	deviceWriter    *kafka.Writer
	wg              sync.WaitGroup
}

// NewKafkaProducer creates a new Kafka producer with writers for both topics.
func NewKafkaProducer(brokerURL string) *KafkaProducer {
	telemetryWriter := &kafka.Writer{
		Addr:         kafka.TCP(brokerURL),
		Topic:        "device.telemetry",
		Balancer:     &kafka.LeastBytes{},
		BatchTimeout: 10 * time.Millisecond,
		RequiredAcks: kafka.RequireOne,
	}
	deviceWriter := &kafka.Writer{
		Addr:         kafka.TCP(brokerURL),
		Topic:        "device.events",
		Balancer:     &kafka.LeastBytes{},
		BatchTimeout: 10 * time.Millisecond,
		RequiredAcks: kafka.RequireOne,
	}
	return &KafkaProducer{
		telemetryWriter: telemetryWriter,
		deviceWriter:    deviceWriter,
	}
}

// NewTelemetryEvent creates a TelemetryEvent with generated event_id and a
// deterministic device UUID derived from the integer sensor ID.
func NewTelemetryEvent(sensorID int, houseID string, metricName string, value float64, unit string) TelemetryEvent {
	return TelemetryEvent{
		EventID:    uuid.New().String(),
		DeviceID:   fmt.Sprintf("00000000-0000-0000-0000-%012d", sensorID),
		HouseID:    houseID,
		MetricName: metricName,
		Value:      value,
		Unit:       unit,
		Timestamp:  time.Now().UTC().Format(time.RFC3339),
	}
}

// PublishTelemetry publishes a telemetry event to Kafka.
func (p *KafkaProducer) PublishTelemetry(ctx context.Context, event TelemetryEvent) error {
	data, err := json.Marshal(event)
	if err != nil {
		return err
	}

	msg := kafka.Message{
		Key:   []byte(event.DeviceID),
		Value: data,
	}

	if err = p.telemetryWriter.WriteMessages(ctx, msg); err != nil {
		log.Printf("Failed to publish telemetry to Kafka: %v", err)
		return err
	}

	log.Printf("Published telemetry to Kafka for device %s: %s = %.1f %s",
		event.DeviceID, event.MetricName, event.Value, event.Unit)
	return nil
}

// PublishTelemetryAsync publishes a telemetry event in a background goroutine,
// tracked by the internal WaitGroup so Close() can wait for completion.
func (p *KafkaProducer) PublishTelemetryAsync(event TelemetryEvent) {
	p.wg.Add(1)
	go func() {
		defer p.wg.Done()
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		if err := p.PublishTelemetry(ctx, event); err != nil {
			log.Printf("Failed to publish telemetry to Kafka: %v", err)
		}
	}()
}

// PublishDeviceEvent publishes a sensor CRUD event to the device.events topic.
func (p *KafkaProducer) PublishDeviceEvent(ctx context.Context, event DeviceEvent) error {
	data, err := json.Marshal(event)
	if err != nil {
		return err
	}

	msg := kafka.Message{
		Key:   []byte(fmt.Sprintf("%d", event.DeviceID)),
		Value: data,
	}

	if err = p.deviceWriter.WriteMessages(ctx, msg); err != nil {
		log.Printf("Failed to publish device event to Kafka: %v", err)
		return err
	}

	log.Printf("Published %s event to Kafka for device %d", event.EventType, event.DeviceID)
	return nil
}

// PublishDeviceEventAsync publishes a device event in a background goroutine,
// tracked by the internal WaitGroup so Close() can wait for completion.
func (p *KafkaProducer) PublishDeviceEventAsync(event DeviceEvent) {
	p.wg.Add(1)
	go func() {
		defer p.wg.Done()
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		if err := p.PublishDeviceEvent(ctx, event); err != nil {
			log.Printf("Failed to publish device event to Kafka: %v", err)
		}
	}()
}

// Close waits for all in-flight publishes to finish, then closes both Kafka writers.
func (p *KafkaProducer) Close() error {
	p.wg.Wait()
	var firstErr error
	if p.telemetryWriter != nil {
		if err := p.telemetryWriter.Close(); err != nil {
			firstErr = err
		}
	}
	if p.deviceWriter != nil {
		if err := p.deviceWriter.Close(); err != nil && firstErr == nil {
			firstErr = err
		}
	}
	return firstErr
}
