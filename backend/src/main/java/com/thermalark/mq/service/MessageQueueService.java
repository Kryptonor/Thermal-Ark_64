package com.thermalark.mq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageQueueService {
    
    @Autowired
    private AmqpTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送热能数据到消息队列
     */
    public boolean sendEnergyData(Map<String, Object> energyData) {
        try {
            String message = objectMapper.writeValueAsString(energyData);
            rabbitTemplate.convertAndSend(
                "energy.data.exchange",
                "energy.data.routing",
                message
            );
            log.info("✅ 热能数据发送成功: {}", energyData.get("device_id"));
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 热能数据发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送交易匹配消息
     */
    public boolean sendTransactionMatch(Map<String, Object> transactionData) {
        try {
            String message = objectMapper.writeValueAsString(transactionData);
            rabbitTemplate.convertAndSend(
                "transaction.match.exchange",
                "transaction.match.routing",
                message
            );
            log.info("✅ 交易匹配消息发送成功: {}", transactionData.get("transaction_id"));
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 交易匹配消息发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送支付通知消息
     */
    public boolean sendPaymentNotify(Map<String, Object> paymentData) {
        try {
            String message = objectMapper.writeValueAsString(paymentData);
            rabbitTemplate.convertAndSend(
                "payment.notify.exchange",
                "payment.notify.routing",
                message
            );
            log.info("✅ 支付通知发送成功: {}", paymentData.get("payment_id"));
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 支付通知发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送区块链交易消息
     */
    public boolean sendBlockchainTransaction(Map<String, Object> blockchainData) {
        try {
            String message = objectMapper.writeValueAsString(blockchainData);
            rabbitTemplate.convertAndSend(
                "blockchain.tx.exchange",
                "blockchain.tx.routing",
                message
            );
            log.info("✅ 区块链交易消息发送成功: {}", blockchainData.get("tx_hash"));
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 区块链交易消息发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送带优先级的消息
     */
    public boolean sendPriorityMessage(String exchange, String routingKey, 
                                      Object data, int priority) {
        try {
            String messageBody = objectMapper.writeValueAsString(data);
            
            Message message = MessageBuilder
                .withBody(messageBody.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setPriority(priority)
                .build();
                
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("✅ 优先级消息发送成功: priority={}", priority);
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 优先级消息发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送延迟消息
     */
    public boolean sendDelayedMessage(String exchange, String routingKey, 
                                     Object data, int delayMillis) {
        try {
            String messageBody = objectMapper.writeValueAsString(data);
            
            Message message = MessageBuilder
                .withBody(messageBody.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("x-delay", delayMillis)
                .build();
                
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("✅ 延迟消息发送成功: delay={}ms", delayMillis);
            return true;
        } catch (JsonProcessingException | AmqpException e) {
            log.error("❌ 延迟消息发送失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 构建热能数据消息
     */
    public Map<String, Object> buildEnergyDataMessage(String deviceId, String userId, 
                                                     double heatProduced, double heatUsed,
                                                     double waterTemperature, double flowRate) {
        Map<String, Object> message = new HashMap<>();
        message.put("device_id", deviceId);
        message.put("user_id", userId);
        message.put("heat_produced", heatProduced);
        message.put("heat_used", heatUsed);
        message.put("water_temperature", waterTemperature);
        message.put("flow_rate", flowRate);
        message.put("timestamp", System.currentTimeMillis());
        message.put("message_type", "energy_data");
        return message;
    }
    
    /**
     * 构建交易匹配消息
     */
    public Map<String, Object> buildTransactionMatchMessage(String transactionId, 
                                                           String buyerId, String sellerId,
                                                           double energyAmount, double price) {
        Map<String, Object> message = new HashMap<>();
        message.put("transaction_id", transactionId);
        message.put("buyer_id", buyerId);
        message.put("seller_id", sellerId);
        message.put("energy_amount", energyAmount);
        message.put("price", price);
        message.put("timestamp", System.currentTimeMillis());
        message.put("message_type", "transaction_match");
        return message;
    }
    
    /**
     * 构建支付通知消息
     */
    public Map<String, Object> buildPaymentNotifyMessage(String paymentId, String orderId,
                                                        double amount, String paymentMethod,
                                                        String status) {
        Map<String, Object> message = new HashMap<>();
        message.put("payment_id", paymentId);
        message.put("order_id", orderId);
        message.put("amount", amount);
        message.put("payment_method", paymentMethod);
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        message.put("message_type", "payment_notify");
        return message;
    }
    
    /**
     * 构建区块链交易消息
     */
    public Map<String, Object> buildBlockchainTransactionMessage(String txHash, 
                                                               String contractAddress,
                                                               String functionName,
                                                               Object[] parameters) {
        Map<String, Object> message = new HashMap<>();
        message.put("tx_hash", txHash);
        message.put("contract_address", contractAddress);
        message.put("function_name", functionName);
        message.put("parameters", parameters);
        message.put("timestamp", System.currentTimeMillis());
        message.put("message_type", "blockchain_transaction");
        return message;
    }
}