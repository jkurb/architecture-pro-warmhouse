package com.smarthome.telemetryservice.kafka;

import com.smarthome.telemetryservice.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryConsumer {

    private final TelemetryService telemetryService;

    @KafkaListener(topics = "device.telemetry", groupId = "telemetry-service-group")
    public void consumeTelemetry(Map<String, Object> message) {
        try {
            Integer deviceId = ((Number) message.get("device_id")).intValue();
            String metricName = (String) message.get("metric_name");
            Double value = ((Number) message.get("value")).doubleValue();
            String unit = (String) message.get("unit");

            telemetryService.recordFromKafka(deviceId, metricName, value, unit);
            log.info("Consumed telemetry event for device {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to process telemetry event: {}", e.getMessage(), e);
        }
    }
}
