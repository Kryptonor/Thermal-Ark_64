package com.thermalark.service;

import com.thermalark.dto.PaymentRequest;
import com.thermalark.dto.PaymentResponse;
import com.thermalark.payment.service.WechatPayService;
import com.thermalark.payment.service.AlipayService;
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
class PaymentServiceTest {

    @Mock
    private WechatPayService wechatPayService;

    @Mock
    private AlipayService alipayService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId("user-001");
        paymentRequest.setAmount(100.0);
        paymentRequest.setPaymentMethod("WECHAT");
        paymentRequest.setOrderId("order-001");
    }

    @Test
    void testProcessPayment_WechatPay_Success() {
        // 模拟微信支付成功
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setSuccess(true);
        mockResponse.setPaymentId("wxpay-001");
        mockResponse.setQrCode("wechat_qr_code_url");
        
        when(wechatPayService.createPayment(any(PaymentRequest.class))).thenReturn(mockResponse);

        // 执行测试
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("wxpay-001", response.getPaymentId());
        assertEquals("wechat_qr_code_url", response.getQrCode());

        // 验证依赖调用
        verify(wechatPayService, times(1)).createPayment(any(PaymentRequest.class));
        verify(alipayService, never()).createPayment(any(PaymentRequest.class));
    }

    @Test
    void testProcessPayment_Alipay_Success() {
        // 设置支付宝支付
        paymentRequest.setPaymentMethod("ALIPAY");
        
        // 模拟支付宝支付成功
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setSuccess(true);
        mockResponse.setPaymentId("alipay-001");
        mockResponse.setQrCode("alipay_qr_code_url");
        
        when(alipayService.createPayment(any(PaymentRequest.class))).thenReturn(mockResponse);

        // 执行测试
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("alipay-001", response.getPaymentId());
        assertEquals("alipay_qr_code_url", response.getQrCode());

        // 验证依赖调用
        verify(alipayService, times(1)).createPayment(any(PaymentRequest.class));
        verify(wechatPayService, never()).createPayment(any(PaymentRequest.class));
    }

    @Test
    void testProcessPayment_InvalidPaymentMethod() {
        // 设置无效支付方式
        paymentRequest.setPaymentMethod("INVALID");

        // 执行测试
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("不支持"));

        // 验证依赖调用
        verify(wechatPayService, never()).createPayment(any(PaymentRequest.class));
        verify(alipayService, never()).createPayment(any(PaymentRequest.class));
    }

    @Test
    void testProcessPayment_WechatPay_Failure() {
        // 模拟微信支付失败
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setSuccess(false);
        mockResponse.setMessage("支付失败：余额不足");
        
        when(wechatPayService.createPayment(any(PaymentRequest.class))).thenReturn(mockResponse);

        // 执行测试
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("支付失败：余额不足", response.getMessage());

        // 验证依赖调用
        verify(wechatPayService, times(1)).createPayment(any(PaymentRequest.class));
    }

    @Test
    void testQueryPaymentStatus_Success() {
        // 模拟查询支付状态成功
        String paymentId = "wxpay-001";
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setSuccess(true);
        mockResponse.setPaymentStatus("SUCCESS");
        
        when(wechatPayService.queryPaymentStatus(paymentId)).thenReturn(mockResponse);

        // 执行测试
        PaymentResponse response = paymentService.queryPaymentStatus(paymentId, "WECHAT");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("SUCCESS", response.getPaymentStatus());

        // 验证依赖调用
        verify(wechatPayService, times(1)).queryPaymentStatus(paymentId);
    }

    @Test
    void testRefundPayment_Success() {
        // 模拟退款成功
        String paymentId = "wxpay-001";
        double amount = 50.0;
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setSuccess(true);
        mockResponse.setMessage("退款成功");
        
        when(wechatPayService.refundPayment(paymentId, amount)).thenReturn(mockResponse);

        // 执行测试
        PaymentResponse response = paymentService.refundPayment(paymentId, amount, "WECHAT");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("退款成功", response.getMessage());

        // 验证依赖调用
        verify(wechatPayService, times(1)).refundPayment(paymentId, amount);
    }
}