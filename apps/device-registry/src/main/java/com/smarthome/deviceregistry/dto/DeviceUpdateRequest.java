package com.smarthome.deviceregistry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateRequest {

    private String name;
    private String type;
    private String location;
    private String serialNumber;
    private String status;
}
