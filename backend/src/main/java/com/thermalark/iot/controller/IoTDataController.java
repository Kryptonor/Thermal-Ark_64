package com.thermalark.iot.controller;

import com.thermalark.iot.dto.IoTDataRequest;
import com.thermalark.iot.dto.IoTDataResponse;
import com.thermalark.mq.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
@Slf4j
public class IoTDataController {
    
    @Autowired
    private MessageQueueService messageQueueService;
    
    /**
     * 接收IoT设备数据
     */
    @PostMapping("/data")
    public ResponseEntity<IoTDataResponse> receiveData(@Valid @RequestBody IoTDataRequest request) {
        try {
            log.info("📡 接收到IoT数据: device_id={}, user_id={}", 
                    request.getDeviceId(), request.getUserId());
            
            // 构建热能数据消息
            Map<String, Object> energyData = messageQueueService.buildEnergyDataMessage(
                request.getDeviceId(),
                request.getUserId(),
                request.getHeatProduced(),
                request.getHeatUsed(),
                request.getWaterTemperature(),
                request.getFlowRate()
            );
            
            // 发送到消息队列
            boolean success = messageQueueService.sendEnergyData(energyData);
            
            IoTDataResponse response = new IoTDataResponse();
            response.setSuccess(success);
            response.setMessage(success ? "数据接收成功" : "数据发送失败");
            response.setTimestamp(System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ IoT数据接收异常: {}", e.getMessage());
            IoTDataResponse response = new IoTDataResponse();
            response.setSuccess(false);
            response.setMessage("数据处理异常: " + e.getMessage());
            response.setTimestamp(System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取设备状态
     */
    @GetMapping("/device/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> getDeviceStatus(@PathVariable String deviceId) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("device_id", deviceId);
            status.put("status", "online");
            status.put("last_heartbeat", System.currentTimeMillis());
            status.put("message", "设备运行正常");
            
            log.info("📱 查询设备状态: device_id={}", deviceId);
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("❌ 查询设备状态异常: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 设备心跳检测
     */
    @PostMapping("/device/{deviceId}/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@PathVariable String deviceId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("device_id", deviceId);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "alive");
            response.put("message", "心跳检测正常");
            
            log.info("💓 设备心跳检测: device_id={}", deviceId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 设备心跳检测异常: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "心跳检测失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 批量接收设备数据
     */
    @PostMapping("/data/batch")
    public ResponseEntity<Map<String, Object>> receiveBatchData(@Valid @RequestBody IoTDataRequest[] requests) {
        try {
            log.info("📦 批量接收IoT数据: count={}", requests.length);
            
            int successCount = 0;
            int failCount = 0;
            
            for (IoTDataRequest request : requests) {
                try {
                    Map<String, Object> energyData = messageQueueService.buildEnergyDataMessage(
                        request.getDeviceId(),
                        request.getUserId(),
                        request.getHeatProduced(),
                        request.getHeatUsed(),
                        request.getWaterTemperature(),
                        request.getFlowRate()
                    );
                    
                    if (messageQueueService.sendEnergyData(energyData)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("❌ 批量数据处理异常: device_id={}, error={}", 
                            request.getDeviceId(), e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", requests.length);
            response.put("success", successCount);
            response.put("failed", failCount);
            response.put("timestamp", System.currentTimeMillis());
            response.put("message", String.format("批量处理完成: 成功%d条, 失败%d条", successCount, failCount));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 批量数据接收异常: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "批量处理异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}