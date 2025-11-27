package com.thermalark.payment.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderId;
    
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod; // WECHAT_PAY, ALIPAY
    
    @NotBlank(message = "商品描述不能为空")
    private String description;
    
    private String notifyUrl;
    private String returnUrl;
    private String attach; // 附加数据
}