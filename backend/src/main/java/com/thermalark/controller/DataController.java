package com.thermalark.controller;

import com.thermalark.entity.IoTData;
import com.thermalark.repository.IoTDataRepository;
import com.thermalark.service.BlockchainService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {
    
    private final IoTDataRepository ioTDataRepository;
    private final BlockchainService blockchainService;
    
    /**
     * 获取能源数据
     */
    @GetMapping("/energy")
    public ResponseEntity<?> getEnergyData(@RequestParam(required = false) String deviceId,
                                          @RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) LocalDateTime startTime,
                                          @RequestParam(required = false) LocalDateTime endTime) {
        try {
            List<IoTData> data;
            
            if (deviceId != null && startTime != null && endTime != null) {
                // 按设备ID和时间范围查询
                data = ioTDataRepository.findByDeviceIdAndTimeRange(deviceId, startTime, endTime);
            } else if (userId != null) {
                // 按用户ID查询
                data = ioTDataRepository.findByUserId(userId);
            } else if (startTime != null && endTime != null) {
                // 按时间范围查询活跃数据
                data = ioTDataRepository.findActiveDataByTimeRange(startTime, endTime);
            } else {
                // 获取所有数据（需要管理员权限）
                data = ioTDataRepository.findAll();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("total", data.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取能源数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取社区数据统计
     */
    @GetMapping("/community")
    public ResponseEntity<?> getCommunityData() {
        try {
            // 获取活跃设备数量
            long activeDevices = ioTDataRepository.findAll().stream()
                    .filter(IoTData::getIsActive)
                    .map(IoTData::getDeviceId)
                    .distinct()
                    .count();
            
            // 获取总能源产出
            BigDecimal totalEnergyOutput = ioTDataRepository.findAll().stream()
                    .map(IoTData::getEnergyOutput)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 获取平均温度
            BigDecimal averageTemperature = ioTDataRepository.findAll().stream()
                    .map(IoTData::getTemperature)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (!ioTDataRepository.findAll().isEmpty()) {
                averageTemperature = averageTemperature.divide(
                    BigDecimal.valueOf(ioTDataRepository.findAll().size()), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            // 获取区块链信息
            BlockchainService.BlockchainInfo blockchainInfo = blockchainService.getBlockchainInfo();
            
            CommunityStats stats = new CommunityStats();
            stats.setActiveDevices(activeDevices);
            stats.setTotalEnergyOutput(totalEnergyOutput);
            stats.setAverageTemperature(averageTemperature);
            stats.setBlockchainConnected(blockchainInfo.isConnected());
            stats.setTotalTransactions(0L); // 这里应该从交易服务获取
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取社区数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 添加物联网数据
     */
    @PostMapping("/iot")
    public ResponseEntity<?> addIoTData(@Valid @RequestBody IoTDataRequest request) {
        try {
            IoTData data = new IoTData();
            data.setDeviceId(request.getDeviceId());
            data.setTemperature(request.getTemperature());
            data.setEnergyOutput(request.getEnergyOutput());
            data.setEnergyConsumption(request.getEnergyConsumption());
            data.setEfficiency(request.getEfficiency());
            data.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
            data.setIsActive(true);
            
            IoTData savedData = ioTDataRepository.save(data);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "物联网数据添加成功");
            response.put("data", savedData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加物联网数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取设备统计数据
     */
    @GetMapping("/device/{deviceId}/stats")
    public ResponseEntity<?> getDeviceStats(@PathVariable String deviceId,
                                           @RequestParam(required = false) LocalDateTime startTime,
                                           @RequestParam(required = false) LocalDateTime endTime) {
        try {
            LocalDateTime defaultStartTime = startTime != null ? startTime : LocalDateTime.now().minusDays(7);
            LocalDateTime defaultEndTime = endTime != null ? endTime : LocalDateTime.now();
            
            BigDecimal averageTemperature = ioTDataRepository.getAverageTemperature(deviceId, defaultStartTime, defaultEndTime);
            BigDecimal totalEnergyOutput = ioTDataRepository.getTotalEnergyOutput(deviceId, defaultStartTime, defaultEndTime);
            
            List<IoTData> recentData = ioTDataRepository.findByDeviceIdAndTimeRange(deviceId, 
                    defaultStartTime, defaultEndTime);
            
            DeviceStats stats = new DeviceStats();
            stats.setDeviceId(deviceId);
            stats.setAverageTemperature(averageTemperature != null ? averageTemperature : BigDecimal.ZERO);
            stats.setTotalEnergyOutput(totalEnergyOutput != null ? totalEnergyOutput : BigDecimal.ZERO);
            stats.setDataPoints(recentData.size());
            stats.setStartTime(defaultStartTime);
            stats.setEndTime(defaultEndTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取设备统计数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Data
    public static class IoTDataRequest {
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;
        
        private BigDecimal temperature;
        private BigDecimal energyOutput;
        private BigDecimal energyConsumption;
        private BigDecimal efficiency;
        private LocalDateTime timestamp;
    }
    
    @Data
    public static class CommunityStats {
        private Long activeDevices;
        private BigDecimal totalEnergyOutput;
        private BigDecimal averageTemperature;
        private Boolean blockchainConnected;
        private Long totalTransactions;
    }
    
    @Data
    public static class DeviceStats {
        private String deviceId;
        private BigDecimal averageTemperature;
        private BigDecimal totalEnergyOutput;
        private Integer dataPoints;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}