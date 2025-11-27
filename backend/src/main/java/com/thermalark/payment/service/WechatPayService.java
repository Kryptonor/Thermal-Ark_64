package com.thermalark.payment.service;

import com.thermalark.payment.dto.PaymentRequest;
import com.thermalark.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class WechatPayService {
    
    @Value("${wechat.pay.appid:wx1234567890abcdef}")
    private String appId;
    
    @Value("${wechat.pay.mchid:1230000109}")
    private String mchId;
    
    @Value("${wechat.pay.key:your-wechat-pay-key}")
    private String apiKey;
    
    @Value("${wechat.pay.notify-url:http://localhost:8080/api/payment/wechat/notify}")
    private String notifyUrl;
    
    /**
     * 创建微信支付订单
     */
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("创建微信支付订单: {}", request.getOrderId());
            
            // 模拟微信支付API调用
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setMessage("微信支付订单创建成功");
            response.setPaymentId(generatePaymentId());
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setPaymentMethod("WECHAT_PAY");
            response.setQrCodeUrl(generateQrCodeUrl(request));
            response.setPaymentUrl("https://pay.weixin.qq.com/simulate/payment");
            response.setStatus("PENDING");
            response.setTimestamp(System.currentTimeMillis());
            
            log.info("微信支付订单创建成功: {}", response.getPaymentId());
            return response;
            
        } catch (Exception e) {
            log.error("创建微信支付订单失败: {}", e.getMessage(), e);
            return createErrorResponse("微信支付订单创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证支付结果
     */
    public boolean verifyPayment(String transactionId) {
        try {
            log.info("验证微信支付结果: {}", transactionId);
            
            // 模拟验证逻辑
            // 在实际应用中，这里应该调用微信支付API查询订单状态
            Thread.sleep(100); // 模拟网络延迟
            
            // 模拟验证结果（80%成功，20%失败）
            boolean success = new SecureRandom().nextInt(100) < 80;
            log.info("微信支付验证结果: {} - {}", transactionId, success ? "SUCCESS" : "FAILED");
            
            return success;
            
        } catch (Exception e) {
            log.error("验证微信支付失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查询支付状态
     */
    public String queryPaymentStatus(String paymentId) {
        try {
            log.info("查询微信支付状态: {}", paymentId);
            
            // 模拟查询逻辑
            String[] statuses = {"PENDING", "SUCCESS", "FAILED"};
            String status = statuses[new SecureRandom().nextInt(statuses.length)];
            
            log.info("微信支付状态查询结果: {} - {}", paymentId, status);
            return status;
            
        } catch (Exception e) {
            log.error("查询微信支付状态失败: {}", e.getMessage(), e);
            return "FAILED";
        }
    }
    
    /**
     * 处理支付回调
     */
    public boolean handlePaymentCallback(Map<String, String> callbackData) {
        try {
            log.info("处理微信支付回调: {}", callbackData);
            
            // 验证签名
            if (!verifySignature(callbackData)) {
                log.error("微信支付回调签名验证失败");
                return false;
            }
            
            // 处理回调数据
            String resultCode = callbackData.get("result_code");
            boolean success = "SUCCESS".equals(resultCode);
            
            log.info("微信支付回调处理结果: {}", success ? "SUCCESS" : "FAILED");
            return success;
            
        } catch (Exception e) {
            log.error("处理微信支付回调失败: {}", e.getMessage(), e);
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
        return "WX" + timestamp + random;
    }
    
    /**
     * 生成二维码URL
     */
    private String generateQrCodeUrl(PaymentRequest request) {
        // 模拟生成二维码URL
        String baseUrl = "https://api.weixin.qq.com/simulate/qrcode";
        String params = String.format("?order_id=%s&amount=%s&desc=%s", 
            request.getOrderId(), 
            request.getAmount().toString(),
            encodeUrl(request.getDescription()));
        return baseUrl + params;
    }
    
    /**
     * 验证签名
     */
    private boolean verifySignature(Map<String, String> data) {
        try {
            // 模拟签名验证
            String sign = data.get("sign");
            return sign != null && sign.length() == 32; // 模拟MD5签名长度
        } catch (Exception e) {
            log.error("签名验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成HMAC-SHA256签名
     */
    private String generateSignature(Map<String, String> data, String key) {
        try {
            List<String> keys = new ArrayList<>(data.keySet());
            Collections.sort(keys);
            
            StringBuilder sb = new StringBuilder();
            for (String k : keys) {
                if (!"sign".equals(k) && data.get(k) != null && !data.get(k).isEmpty()) {
                    sb.append(k).append("=").append(data.get(k)).append("&");
                }
            }
            sb.append("key=").append(key);
            
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(secretKey);
            
            byte[] hash = sha256.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).toUpperCase();
            
        } catch (Exception e) {
            log.error("生成签名失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 字节数组转十六进制
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * URL编码
     */
    private String encodeUrl(String text) {
        try {
            return java.net.URLEncoder.encode(text, StandardCharsets.UTF_8.name());
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