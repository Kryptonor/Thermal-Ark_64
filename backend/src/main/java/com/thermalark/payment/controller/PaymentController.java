package com.thermalark.payment.controller;

import com.thermalark.payment.dto.PaymentRequest;
import com.thermalark.payment.dto.PaymentResponse;
import com.thermalark.payment.service.AlipayService;
import com.thermalark.payment.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {
    
    @Autowired
    private WechatPayService wechatPayService;
    
    @Autowired
    private AlipayService alipayService;
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            log.info("创建支付订单: {} - {}", request.getPaymentMethod(), request.getOrderId());
            
            PaymentResponse response;
            switch (request.getPaymentMethod().toUpperCase()) {
                case "WECHAT_PAY":
                    response = wechatPayService.createPayment(request);
                    break;
                case "ALIPAY":
                    response = alipayService.createPayment(request);
                    break;
                default:
                    response = createErrorResponse("不支持的支付方式: " + request.getPaymentMethod());
                    break;
            }
            
            if (response.isSuccess()) {
                log.info("支付订单创建成功: {}", response.getPaymentId());
                return ResponseEntity.ok(response);
            } else {
                log.error("支付订单创建失败: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("创建支付订单异常: {}", e.getMessage(), e);
            PaymentResponse errorResponse = createErrorResponse("创建支付订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 查询支付状态
     */
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Map<String, Object>> queryPaymentStatus(
            @PathVariable String paymentId,
            @RequestParam String paymentMethod) {
        
        try {
            log.info("查询支付状态: {} - {}", paymentMethod, paymentId);
            
            String status;
            switch (paymentMethod.toUpperCase()) {
                case "WECHAT_PAY":
                    status = wechatPayService.queryPaymentStatus(paymentId);
                    break;
                case "ALIPAY":
                    status = alipayService.queryPaymentStatus(paymentId);
                    break;
                default:
                    status = "UNKNOWN";
                    break;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", paymentId);
            result.put("paymentMethod", paymentMethod);
            result.put("status", status);
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("支付状态查询结果: {} - {}", paymentId, status);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询支付状态异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "查询支付状态失败: " + e.getMessage());
            errorResult.put("paymentId", paymentId);
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 微信支付回调接口
     */
    @PostMapping("/wechat/notify")
    public ResponseEntity<String> handleWechatNotify(@RequestBody Map<String, String> notifyData) {
        try {
            log.info("收到微信支付回调: {}", notifyData);
            
            boolean success = wechatPayService.handlePaymentCallback(notifyData);
            
            if (success) {
                log.info("微信支付回调处理成功");
                return ResponseEntity.ok("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
            } else {
                log.error("微信支付回调处理失败");
                return ResponseEntity.ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>");
            }
            
        } catch (Exception e) {
            log.error("处理微信支付回调异常: {}", e.getMessage(), e);
            return ResponseEntity.ok("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[ERROR]]></return_msg></xml>");
        }
    }
    
    /**
     * 支付宝回调接口
     */
    @PostMapping("/alipay/notify")
    public ResponseEntity<String> handleAlipayNotify(@RequestBody Map<String, String> notifyData) {
        try {
            log.info("收到支付宝支付回调: {}", notifyData);
            
            boolean success = alipayService.handlePaymentCallback(notifyData);
            
            if (success) {
                log.info("支付宝支付回调处理成功");
                return ResponseEntity.ok("success");
            } else {
                log.error("支付宝支付回调处理失败");
                return ResponseEntity.ok("failure");
            }
            
        } catch (Exception e) {
            log.error("处理支付宝支付回调异常: {}", e.getMessage(), e);
            return ResponseEntity.ok("failure");
        }
    }
    
    /**
     * 验证支付结果
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam String transactionId,
            @RequestParam String paymentMethod) {
        
        try {
            log.info("验证支付结果: {} - {}", paymentMethod, transactionId);
            
            boolean success;
            switch (paymentMethod.toUpperCase()) {
                case "WECHAT_PAY":
                    success = wechatPayService.verifyPayment(transactionId);
                    break;
                case "ALIPAY":
                    success = alipayService.verifyPayment(transactionId);
                    break;
                default:
                    success = false;
                    break;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("transactionId", transactionId);
            result.put("paymentMethod", paymentMethod);
            result.put("verified", success);
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("支付验证结果: {} - {}", transactionId, success ? "SUCCESS" : "FAILED");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("验证支付结果异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "验证支付结果失败: " + e.getMessage());
            errorResult.put("transactionId", transactionId);
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 创建错误响应
     */
    private PaymentResponse createErrorResponse(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus("FAILED");
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}