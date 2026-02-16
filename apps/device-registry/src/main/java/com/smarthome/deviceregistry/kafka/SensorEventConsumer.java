package com.smarthome.deviceregistry.kafka;

import com.smarthome.deviceregistry.model.Device;
import com.smarthome.deviceregistry.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Consumes sensor CRUD events published by the monolith (smart_home) to the
 * device.events topic and synchronises the devices table accordingly.
 * <p>
 * Event types handled: SENSOR_CREATED, SENSOR_UPDATED, SENSOR_DELETED.
 * Events with other types (e.g. DEVICE_CREATED from this service) are ignored.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SensorEventConsumer {

    private final DeviceRepository deviceRepository;

    @KafkaListener(topics = "device.events", groupId = "device-registry-group")
    public void consumeDeviceEvent(Map<String, Object> message) {
        try {
            String eventType = (String) message.get("event_type");
            if (eventType == null || !eventType.startsWith("SENSOR_")) {
                return;
            }

            Integer deviceId = ((Number) message.get("device_id")).intValue();
            String deviceName = (String) message.get("device_name");
            String deviceType = (String) message.get("device_type");
            String location = (String) message.get("location");
            String status = (String) message.get("status");

            switch (eventType) {
                case "SENSOR_CREATED" -> handleSensorCreated(deviceId, deviceName, deviceType, location, status);
                case "SENSOR_UPDATED" -> handleSensorUpdated(deviceId, deviceName, deviceType, location, status);
                case "SENSOR_DELETED" -> handleSensorDeleted(deviceId);
                default -> log.warn("Unknown sensor event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process device event: {}", e.getMessage(), e);
        }
    }

    private void handleSensorCreated(Integer id, String name, String type, String location, String status) {
        Optional<Device> existing = deviceRepository.findById(id);
        if (existing.isPresent()) {
            log.info("Device {} already exists, skipping SENSOR_CREATED", id);
            return;
        }

        Device device = new Device();
        device.setName(name);
        device.setType(type);
        device.setLocation(location);
        device.setStatus(status != null ? status : "offline");

        Device saved = deviceRepository.save(device);
        log.info("Created device {} from SENSOR_CREATED event (monolith sensor {})", saved.getId(), id);
    }

    private void handleSensorUpdated(Integer id, String name, String type, String location, String status) {
        Optional<Device> existing = deviceRepository.findById(id);
        if (existing.isEmpty()) {
            log.warn("Device {} not found for SENSOR_UPDATED, creating new", id);
            handleSensorCreated(id, name, type, location, status);
            return;
        }

        Device device = existing.get();
        if (name != null) device.setName(name);
        if (type != null) device.setType(type);
        if (location != null) device.setLocation(location);
        if (status != null) device.setStatus(status);

        deviceRepository.save(device);
        log.info("Updated device {} from SENSOR_UPDATED event", id);
    }

    private void handleSensorDeleted(Integer id) {
        Optional<Device> existing = deviceRepository.findById(id);
        if (existing.isEmpty()) {
            log.warn("Device {} not found for SENSOR_DELETED, ignoring", id);
            return;
        }

        deviceRepository.deleteById(id);
        log.info("Deleted device {} from SENSOR_DELETED event", id);
    }
}
