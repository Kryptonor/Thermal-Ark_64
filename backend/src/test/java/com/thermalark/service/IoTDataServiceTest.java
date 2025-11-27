package com.thermalark.service;

import com.thermalark.iot.dto.IoTDataRequest;
import com.thermalark.iot.dto.IoTDataResponse;
import com.thermalark.mq.service.MessageQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class IoTDataServiceTest {

    @Mock
    private MessageQueueService messageQueueService;

    @InjectMocks
    private IoTDataService iotDataService;

    private IoTDataRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new IoTDataRequest();
        testRequest.setDeviceId("smart-heat-meter-001");
        testRequest.setUserId("user-001");
        testRequest.setHeatProduction(150.5);
        testRequest.setHeatConsumption(120.3);
        testRequest.setWaterTemperature(65.0);
        testRequest.setFlowRate(2.5);
    }

    @Test
    void testProcessIoTData_Success() {
        // 模拟消息队列服务
        doNothing().when(messageQueueService).sendEnergyData(any());

        // 执行测试
        IoTDataResponse response = iotDataService.processIoTData(testRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("数据接收成功", response.getMessage());
        assertNotNull(response.getDataId());
        assertEquals("smart-heat-meter-001", response.getDeviceId());

        // 验证消息队列被调用
        verify(messageQueueService, times(1)).sendEnergyData(any());
    }

    @Test
    void testProcessIoTData_InvalidData() {
        // 设置无效数据
        testRequest.setHeatProduction(-10.0);

        // 执行测试
        IoTDataResponse response = iotDataService.processIoTData(testRequest);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("无效"));

        // 验证消息队列没有被调用
        verify(messageQueueService, never()).sendEnergyData(any());
    }

    @Test
    void testGetDeviceStatus_Online() {
        // 模拟在线设备
        String deviceId = "smart-heat-meter-001";
        
        // 执行测试
        String status = iotDataService.getDeviceStatus(deviceId);

        // 验证结果
        assertNotNull(status);
        assertEquals("online", status);
    }

    @Test
    void testGetDeviceStatus_Offline() {
        // 模拟离线设备
        String deviceId = "non-existent-device";
        
        // 执行测试
        String status = iotDataService.getDeviceStatus(deviceId);

        // 验证结果
        assertNotNull(status);
        assertEquals("offline", status);
    }
}