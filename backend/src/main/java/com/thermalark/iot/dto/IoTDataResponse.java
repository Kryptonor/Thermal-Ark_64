package com.thermalark.iot.dto;

import lombok.Data;

@Data
public class IoTDataResponse {
    
    private Boolean success;
    private String message;
    private Long timestamp;
    private String dataId;
    private String deviceId;
    private String status;
    
    public IoTDataResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public IoTDataResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public IoTDataResponse(Boolean success, String message, String dataId, String deviceId) {
        this.success = success;
        this.message = message;
        this.dataId = dataId;
        this.deviceId = deviceId;
        this.timestamp = System.currentTimeMillis();
    }
}