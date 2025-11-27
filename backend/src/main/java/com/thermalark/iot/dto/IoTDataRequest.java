package com.thermalark.iot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class IoTDataRequest {
    
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotNull(message = "产热量不能为空")
    @PositiveOrZero(message = "产热量必须大于等于0")
    private Double heatProduced;
    
    @NotNull(message = "用热量不能为空")
    @PositiveOrZero(message = "用热量必须大于等于0")
    private Double heatUsed;
    
    @NotNull(message = "水温不能为空")
    @PositiveOrZero(message = "水温必须大于等于0")
    private Double waterTemperature;
    
    @NotNull(message = "流量不能为空")
    @PositiveOrZero(message = "流量必须大于等于0")
    private Double flowRate;
    
    private String location;
    private String deviceType;
    private String firmwareVersion;
    private Long timestamp;
}