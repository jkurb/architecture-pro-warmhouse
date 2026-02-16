package com.smarthome.deviceregistry.service;

import com.smarthome.deviceregistry.dto.DeviceCreateRequest;
import com.smarthome.deviceregistry.dto.DeviceUpdateRequest;
import com.smarthome.deviceregistry.model.Device;
import com.smarthome.deviceregistry.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DEVICE_EVENTS_TOPIC = "device.events";

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Integer id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
    }

    public List<Device> getDevicesByType(String type) {
        return deviceRepository.findByType(type);
    }

    public Device createDevice(DeviceCreateRequest request) {
        Device device = new Device();
        device.setName(request.getName());
        device.setType(request.getType());
        device.setLocation(request.getLocation());
        device.setSerialNumber(request.getSerialNumber());
        device.setStatus("online");

        Device saved = deviceRepository.save(device);
        log.info("Created device: {}", saved.getId());

        publishEvent("DEVICE_CREATED", saved);
        return saved;
    }

    public Device updateDevice(Integer id, DeviceUpdateRequest request) {
        Device device = getDeviceById(id);

        if (request.getName() != null) device.setName(request.getName());
        if (request.getType() != null) device.setType(request.getType());
        if (request.getLocation() != null) device.setLocation(request.getLocation());
        if (request.getSerialNumber() != null) device.setSerialNumber(request.getSerialNumber());
        if (request.getStatus() != null) device.setStatus(request.getStatus());

        Device saved = deviceRepository.save(device);
        log.info("Updated device: {}", saved.getId());

        publishEvent("DEVICE_UPDATED", saved);
        return saved;
    }

    public void deleteDevice(Integer id) {
        Device device = getDeviceById(id);
        deviceRepository.delete(device);
        log.info("Deleted device: {}", id);

        publishEvent("DEVICE_DELETED", device);
    }

    private void publishEvent(String eventType, Device device) {
        try {
            Map<String, Object> event = Map.of(
                    "event_type", eventType,
                    "device_id", device.getId(),
                    "device_name", device.getName(),
                    "device_type", device.getType(),
                    "location", device.getLocation(),
                    "status", device.getStatus(),
                    "timestamp", java.time.Instant.now().toString()
            );
            kafkaTemplate.send(DEVICE_EVENTS_TOPIC, device.getId().toString(), event);
            log.info("Published {} event for device {}", eventType, device.getId());
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka: {}", e.getMessage());
        }
    }
}
