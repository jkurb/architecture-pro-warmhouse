package com.smarthome.telemetryservice.repository;

import com.smarthome.telemetryservice.model.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryData, Long> {

    List<TelemetryData> findByDeviceIdOrderByTimestampDesc(Integer deviceId);

    List<TelemetryData> findByDeviceIdAndMetricNameOrderByTimestampDesc(Integer deviceId, String metricName);

    List<TelemetryData> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
            Integer deviceId, Instant from, Instant to);
}
