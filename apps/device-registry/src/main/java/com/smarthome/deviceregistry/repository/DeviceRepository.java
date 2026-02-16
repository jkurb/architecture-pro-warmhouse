package com.smarthome.deviceregistry.repository;

import com.smarthome.deviceregistry.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    List<Device> findByType(String type);

    List<Device> findByLocation(String location);

    List<Device> findByStatus(String status);
}
