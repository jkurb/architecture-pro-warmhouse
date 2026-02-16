package com.smarthome.telemetryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRequest {

    @NotNull(message = "Device ID is required")
    private Integer deviceId;

    @NotBlank(message = "Metric name is required")
    private String metricName;

    @NotNull(message = "Value is required")
    private Double value;

    private String unit;
}
