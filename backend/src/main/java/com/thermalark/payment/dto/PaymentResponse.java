package com.thermalark.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String qrCodeUrl; // 二维码URL
    private String paymentUrl; // 支付页面URL
    private String transactionId;
    private String status; // PENDING, SUCCESS, FAILED
    private Long timestamp;
}