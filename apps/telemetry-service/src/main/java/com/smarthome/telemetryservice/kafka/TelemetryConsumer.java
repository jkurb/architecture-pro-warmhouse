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
            Integer deviceId = parseDeviceId(message.get("device_id"));
            String metricName = (String) message.get("metric_name");
            Double value = ((Number) message.get("value")).doubleValue();
            String unit = (String) message.get("unit");

            telemetryService.recordFromKafka(deviceId, metricName, value, unit);
            log.info("Consumed telemetry event for device {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to process telemetry event: {}", e.getMessage(), e);
        }
    }

    /**
     * Parses device_id which may arrive as a Number (legacy) or a UUID string
     * (current schema: "00000000-0000-0000-0000-000000000001").
     */
    private Integer parseDeviceId(Object raw) {
        if (raw instanceof Number num) {
            return num.intValue();
        }
        String str = raw.toString();
        // Extract the numeric sensor ID from the last UUID segment
        return Integer.parseInt(str.substring(str.lastIndexOf('-') + 1));
    }
}
