package com.smarthome.temperatureapi.controller;

import com.smarthome.temperatureapi.model.TemperatureResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TemperatureController {

    @GetMapping("/temperature")
    public TemperatureResponse getTemperatureByLocation(
            @RequestParam(value = "location", defaultValue = "unknown") String location) {

        double temperature = generateRandomTemperature();

        return new TemperatureResponse(
                temperature,
                "°C",
                Instant.now(),
                location,
                "active",
                "ext-" + location.toLowerCase().replace(" ", "-"),
                "temperature",
                String.format("Temperature reading for %s: %.1f°C", location, temperature)
        );
    }

    @GetMapping("/temperature/{sensorId}")
    public TemperatureResponse getTemperatureBySensorId(
            @PathVariable String sensorId) {

        double temperature = generateRandomTemperature();

        return new TemperatureResponse(
                temperature,
                "°C",
                Instant.now(),
                "sensor-location",
                "active",
                sensorId,
                "temperature",
                String.format("Temperature reading for sensor %s: %.1f°C", sensorId, temperature)
        );
    }

    private double generateRandomTemperature() {
        double temp = ThreadLocalRandom.current().nextDouble(-10.0, 40.0);
        return Math.round(temp * 10.0) / 10.0;
    }
}
