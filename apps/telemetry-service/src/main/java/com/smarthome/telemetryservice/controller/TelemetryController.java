package com.smarthome.telemetryservice.controller;

import com.smarthome.telemetryservice.dto.TelemetryRequest;
import com.smarthome.telemetryservice.model.TelemetryData;
import com.smarthome.telemetryservice.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/{deviceId}")
    public ResponseEntity<List<TelemetryData>> getTelemetry(
            @PathVariable Integer deviceId,
            @RequestParam(required = false) String metricName) {

        List<TelemetryData> data;
        if (metricName != null) {
            data = telemetryService.getTelemetryByDeviceIdAndMetric(deviceId, metricName);
        } else {
            data = telemetryService.getTelemetryByDeviceId(deviceId);
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<TelemetryData> recordTelemetry(
            @Valid @RequestBody TelemetryRequest request) {
        TelemetryData data = telemetryService.recordTelemetry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }
}
