package com.smarthome.temperatureapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureResponse {

    private double value;
    private String unit;
    private Instant timestamp;
    private String location;
    private String status;

    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("sensor_type")
    private String sensorType;

    private String description;
}
