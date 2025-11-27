package com.thermalark.payment.service;

import com.thermalark.payment.dto.PaymentRequest;
import com.thermalark.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AlipayService {
    
    @Value("${alipay.appid:2021000123456789}")
    private String appId;
    
    @Value("${alipay.merchant-private-key:your-private-key}")
    private String merchantPrivateKey;
    
    @Value("${alipay.alipay-public-key:alipay-public-key}")
    private String alipayPublicKey;
    
    @Value("${alipay.notify-url:http://localhost:8080/api/payment/alipay/notify}")
    private String notifyUrl;
    
    @Value("${alipay.return-url:http://localhost:3000/payment/success}")
    private String returnUrl;
    
    /**
     * 创建支付宝支付订单
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("创建支付宝支付订单: {}", request.getOrderId());
            
            // 模拟支付宝API调用
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setMessage("支付宝支付订单创建成功");
            response.setPaymentId(generatePaymentId());
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setPaymentMethod("ALIPAY");
            response.setPaymentUrl(generatePaymentUrl(request));
            response.setStatus("PENDING");
            response.setTimestamp(System.currentTimeMillis());
            
            log.info("支付宝支付订单创建成功: {}", response.getPaymentId());
            return response;
            
        } catch (Exception e) {
            log.error("创建支付宝支付订单失败: {}", e.getMessage(), e);
            return createErrorResponse("支付宝支付订单创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证支付结果
     */
    public boolean verifyPayment(String tradeNo) {
        try {
            log.info("验证支付宝支付结果: {}", tradeNo);
            
            // 模拟验证逻辑
            // 在实际应用中，这里应该调用支付宝API查询订单状态
            Thread.sleep(100); // 模拟网络延迟
            
            // 模拟验证结果（85%成功，15%失败）
            boolean success = new SecureRandom().nextInt(100) < 85;
            log.info("支付宝支付验证结果: {} - {}", tradeNo, success ? "SUCCESS" : "FAILED");
            
            return success;
            
        } catch (Exception e) {
            log.error("验证支付宝支付失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查询支付状态
     */
    public String queryPaymentStatus(String paymentId) {
        try {
            log.info("查询支付宝支付状态: {}", paymentId);
            
            // 模拟查询逻辑
            String[] statuses = {"PENDING", "SUCCESS", "FAILED"};
            String status = statuses[new SecureRandom().nextInt(statuses.length)];
            
            log.info("支付宝支付状态查询结果: {} - {}", paymentId, status);
            return status;
            
        } catch (Exception e) {
            log.error("查询支付宝支付状态失败: {}", e.getMessage(), e);
            return "FAILED";
        }
    }
    
    /**
     * 处理支付回调
     */
    public boolean handlePaymentCallback(Map<String, String> callbackData) {
        try {
            log.info("处理支付宝支付回调: {}", callbackData);
            
            // 验证签名
            if (!verifySignature(callbackData)) {
                log.error("支付宝支付回调签名验证失败");
                return false;
            }
            
            // 处理回调数据
            String tradeStatus = callbackData.get("trade_status");
            boolean success = "TRADE_SUCCESS".equals(tradeStatus) || 
                             "TRADE_FINISHED".equals(tradeStatus);
            
            log.info("支付宝支付回调处理结果: {}", success ? "SUCCESS" : "FAILED");
            return success;
            
        } catch (Exception e) {
            log.error("处理支付宝支付回调失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 生成支付ID
     */
    private String generatePaymentId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String random = String.format("%06d", new SecureRandom().nextInt(1000000));
        return "ALI" + timestamp + random;
    }
    
    /**
     * 生成支付页面URL
     */
    private String generatePaymentUrl(PaymentRequest request) {
        // 模拟生成支付页面URL
        String baseUrl = "https://openapi.alipay.com/gateway.do";
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("method", "alipay.trade.page.pay");
        params.put("charset", "utf-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.put("version", "1.0");
        params.put("biz_content", generateBizContent(request));
        params.put("return_url", returnUrl);
        params.put("notify_url", notifyUrl);
        
        // 模拟签名
        params.put("sign", "mock_signature_for_demo");
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");
        params.forEach((key, value) -> {
            urlBuilder.append(key).append("=").append(encodeUrl(value)).append("&");
        });
        
        return urlBuilder.substring(0, urlBuilder.length() - 1); // 移除最后一个&
    }
    
    /**
     * 生成业务参数
     */
    private String generateBizContent(PaymentRequest request) {
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", request.getOrderId());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("total_amount", request.getAmount().toString());
        bizContent.put("subject", request.getDescription());
        bizContent.put("body", "热能交易系统 - " + request.getDescription());
        bizContent.put("timeout_express", "30m");
        
        // 转换为JSON字符串
        StringBuilder sb = new StringBuilder("{");
        bizContent.forEach((key, value) -> {
            sb.append("\"").append(key).append("\":\"").append(value).append("\",");
        });
        sb.deleteCharAt(sb.length() - 1); // 移除最后一个逗号
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * 验证签名
     */
    private boolean verifySignature(Map<String, String> data) {
        try {
            // 模拟签名验证
            String sign = data.get("sign");
            return sign != null && sign.length() > 0;
        } catch (Exception e) {
            log.error("签名验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * URL编码
     */
    private String encodeUrl(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
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