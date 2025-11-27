package com.thermalark.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermalark.blockchain.service.BlockchainGateway;
import com.thermalark.payment.service.WechatPayService;
import com.thermalark.payment.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MessageConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BlockchainGateway blockchainGateway;
    
    @Autowired
    private WechatPayService wechatPayService;
    
    @Autowired
    private AlipayService alipayService;
    
    /**
     * 消费热能数据消息
     */
    @RabbitListener(queues = "energy.data")
    public void consumeEnergyData(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            log.info("📊 接收到热能数据: device_id={}, heat_produced={}", 
                    data.get("device_id"), data.get("heat_produced"));
            
            // 处理热能数据逻辑
            processEnergyData(data);
            
        } catch (Exception e) {
            log.error("❌ 处理热能数据失败: {}", e.getMessage());
        }
    }
    
    /**
     * 消费交易匹配消息
     */
    @RabbitListener(queues = "transaction.match")
    public void consumeTransactionMatch(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            log.info("💰 接收到交易匹配消息: transaction_id={}, amount={}", 
                    data.get("transaction_id"), data.get("energy_amount"));
            
            // 处理交易匹配逻辑
            processTransactionMatch(data);
            
        } catch (Exception e) {
            log.error("❌ 处理交易匹配消息失败: {}", e.getMessage());
        }
    }
    
    /**
     * 消费支付通知消息
     */
    @RabbitListener(queues = "payment.notify")
    public void consumePaymentNotify(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            log.info("💳 接收到支付通知: payment_id={}, status={}", 
                    data.get("payment_id"), data.get("status"));
            
            // 处理支付通知逻辑
            processPaymentNotify(data);
            
        } catch (Exception e) {
            log.error("❌ 处理支付通知失败: {}", e.getMessage());
        }
    }
    
    /**
     * 消费区块链交易消息
     */
    @RabbitListener(queues = "blockchain.tx")
    public void consumeBlockchainTransaction(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            log.info("🔗 接收到区块链交易: tx_hash={}, function={}", 
                    data.get("tx_hash"), data.get("function_name"));
            
            // 处理区块链交易逻辑
            processBlockchainTransaction(data);
            
        } catch (Exception e) {
            log.error("❌ 处理区块链交易失败: {}", e.getMessage());
        }
    }
    
    /**
     * 处理热能数据
     */
    private void processEnergyData(Map<String, Object> data) {
        try {
            String deviceId = (String) data.get("device_id");
            String userId = (String) data.get("user_id");
            double heatProduced = (double) data.get("heat_produced");
            double heatUsed = (double) data.get("heat_used");
            
            // 1. 存储到数据库
            // energyDataRepository.save(energyData);
            
            // 2. 计算用户能耗统计
            // userEnergyService.updateUserEnergyStats(userId, heatUsed);
            
            // 3. 触发区块链记录（如果达到阈值）
            if (heatProduced > 5.0) { // 阈值可配置
                triggerBlockchainRecord(data);
            }
            
            log.info("✅ 热能数据处理完成: device_id={}", deviceId);
            
        } catch (Exception e) {
            log.error("❌ 处理热能数据异常: {}", e.getMessage());
        }
    }
    
    /**
     * 处理交易匹配
     */
    private void processTransactionMatch(Map<String, Object> data) {
        try {
            String transactionId = (String) data.get("transaction_id");
            String buyerId = (String) data.get("buyer_id");
            String sellerId = (String) data.get("seller_id");
            double energyAmount = (double) data.get("energy_amount");
            double price = (double) data.get("price");
            
            // 1. 创建交易订单
            // Transaction transaction = createTransaction(transactionId, buyerId, sellerId, energyAmount, price);
            
            // 2. 触发支付流程
            // triggerPaymentProcess(transaction);
            
            // 3. 记录到区块链
            // recordTransactionOnBlockchain(transaction);
            
            log.info("✅ 交易匹配处理完成: transaction_id={}", transactionId);
            
        } catch (Exception e) {
            log.error("❌ 处理交易匹配异常: {}", e.getMessage());
        }
    }
    
    /**
     * 处理支付通知
     */
    private void processPaymentNotify(Map<String, Object> data) {
        try {
            String paymentId = (String) data.get("payment_id");
            String orderId = (String) data.get("order_id");
            String status = (String) data.get("status");
            
            // 1. 更新支付状态
            // paymentService.updatePaymentStatus(paymentId, status);
            
            // 2. 处理支付结果
            if ("SUCCESS".equals(status)) {
                // 支付成功，完成交易
                // completeTransaction(orderId);
                log.info("✅ 支付成功处理: order_id={}", orderId);
            } else if ("FAILED".equals(status)) {
                // 支付失败，取消交易
                // cancelTransaction(orderId);
                log.warn("⚠️ 支付失败处理: order_id={}", orderId);
            }
            
            log.info("✅ 支付通知处理完成: payment_id={}", paymentId);
            
        } catch (Exception e) {
            log.error("❌ 处理支付通知异常: {}", e.getMessage());
        }
    }
    
    /**
     * 处理区块链交易
     */
    private void processBlockchainTransaction(Map<String, Object> data) {
        try {
            String txHash = (String) data.get("tx_hash");
            String contractAddress = (String) data.get("contract_address");
            String functionName = (String) data.get("function_name");
            Object[] parameters = (Object[]) data.get("parameters");
            
            // 1. 验证交易状态
            // Object result = blockchainGateway.queryTransaction(txHash);
            
            // 2. 更新本地数据库状态
            // updateLocalTransactionStatus(txHash, result);
            
            // 3. 触发后续业务逻辑
            // triggerPostBlockchainActions(functionName, parameters);
            
            log.info("✅ 区块链交易处理完成: tx_hash={}", txHash);
            
        } catch (Exception e) {
            log.error("❌ 处理区块链交易异常: {}", e.getMessage());
        }
    }
    
    /**
     * 触发区块链记录
     */
    private void triggerBlockchainRecord(Map<String, Object> data) {
        try {
            String deviceId = (String) data.get("device_id");
            double heatProduced = (double) data.get("heat_produced");
            
            // 模拟区块链记录
            log.info("🔗 触发区块链记录: device_id={}, heat_produced={}", deviceId, heatProduced);
            
            // 在实际应用中，这里会调用区块链网关
            // blockchainGateway.callContract("contract_address", "recordEnergyProduction", deviceId, heatProduced);
            
        } catch (Exception e) {
            log.error("❌ 触发区块链记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 处理消息消费异常
     */
    private void handleConsumptionError(String queue, String message, Exception e) {
        log.error("❌ 消息消费异常 - Queue: {}, Message: {}, Error: {}", 
                queue, message, e.getMessage());
        
        // 在实际应用中，这里可以添加重试机制或死信队列处理
        // messageRetryService.retryMessage(queue, message);
    }
}