package com.smarthome.telemetryservice.service;

import com.smarthome.telemetryservice.dto.TelemetryRequest;
import com.smarthome.telemetryservice.model.TelemetryData;
import com.smarthome.telemetryservice.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public List<TelemetryData> getTelemetryByDeviceId(Integer deviceId) {
        return telemetryRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<TelemetryData> getTelemetryByDeviceIdAndMetric(Integer deviceId, String metricName) {
        return telemetryRepository.findByDeviceIdAndMetricNameOrderByTimestampDesc(deviceId, metricName);
    }

    public List<TelemetryData> getTelemetryByDeviceIdAndTimeRange(
            Integer deviceId, Instant from, Instant to) {
        return telemetryRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(deviceId, from, to);
    }

    public TelemetryData recordTelemetry(TelemetryRequest request) {
        TelemetryData data = new TelemetryData();
        data.setDeviceId(request.getDeviceId());
        data.setMetricName(request.getMetricName());
        data.setValue(request.getValue());
        data.setUnit(request.getUnit());
        data.setTimestamp(Instant.now());

        TelemetryData saved = telemetryRepository.save(data);
        log.info("Recorded telemetry for device {}: {} = {} {}",
                request.getDeviceId(), request.getMetricName(),
                request.getValue(), request.getUnit());
        return saved;
    }

    public TelemetryData recordFromKafka(Integer deviceId, String metricName,
                                         Double value, String unit) {
        TelemetryData data = new TelemetryData();
        data.setDeviceId(deviceId);
        data.setMetricName(metricName);
        data.setValue(value);
        data.setUnit(unit);
        data.setTimestamp(Instant.now());

        TelemetryData saved = telemetryRepository.save(data);
        log.info("Recorded telemetry from Kafka for device {}: {} = {} {}",
                deviceId, metricName, value, unit);
        return saved;
    }
}
