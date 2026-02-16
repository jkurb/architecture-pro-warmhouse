package com.smarthome.deviceregistry.controller;

import com.smarthome.deviceregistry.dto.DeviceCreateRequest;
import com.smarthome.deviceregistry.dto.DeviceUpdateRequest;
import com.smarthome.deviceregistry.model.Device;
import com.smarthome.deviceregistry.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices(
            @RequestParam(required = false) String type) {
        List<Device> devices;
        if (type != null) {
            devices = deviceService.getDevicesByType(type);
        } else {
            devices = deviceService.getAllDevices();
        }
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Integer id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@Valid @RequestBody DeviceCreateRequest request) {
        Device device = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Integer id, @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.updateDevice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
