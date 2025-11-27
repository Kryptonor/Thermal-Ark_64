package com.thermalark.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // 热能数据队列
    public static final String ENERGY_DATA_QUEUE = "energy.data";
    public static final String ENERGY_DATA_EXCHANGE = "energy.data.exchange";
    public static final String ENERGY_DATA_ROUTING_KEY = "energy.data.routing";
    
    // 交易匹配队列
    public static final String TRANSACTION_QUEUE = "transaction.match";
    public static final String TRANSACTION_EXCHANGE = "transaction.match.exchange";
    public static final String TRANSACTION_ROUTING_KEY = "transaction.match.routing";
    
    // 支付通知队列
    public static final String PAYMENT_NOTIFY_QUEUE = "payment.notify";
    public static final String PAYMENT_NOTIFY_EXCHANGE = "payment.notify.exchange";
    public static final String PAYMENT_NOTIFY_ROUTING_KEY = "payment.notify.routing";
    
    // 区块链交易队列
    public static final String BLOCKCHAIN_TX_QUEUE = "blockchain.tx";
    public static final String BLOCKCHAIN_TX_EXCHANGE = "blockchain.tx.exchange";
    public static final String BLOCKCHAIN_TX_ROUTING_KEY = "blockchain.tx.routing";
    
    /**
     * 热能数据队列配置
     */
    @Bean
    public Queue energyDataQueue() {
        return new Queue(ENERGY_DATA_QUEUE, true); // durable=true
    }
    
    @Bean
    public DirectExchange energyDataExchange() {
        return new DirectExchange(ENERGY_DATA_EXCHANGE);
    }
    
    @Bean
    public Binding energyDataBinding(Queue energyDataQueue, DirectExchange energyDataExchange) {
        return BindingBuilder.bind(energyDataQueue)
                .to(energyDataExchange)
                .with(ENERGY_DATA_ROUTING_KEY);
    }
    
    /**
     * 交易匹配队列配置
     */
    @Bean
    public Queue transactionQueue() {
        return new Queue(TRANSACTION_QUEUE, true);
    }
    
    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(TRANSACTION_EXCHANGE);
    }
    
    @Bean
    public Binding transactionBinding(Queue transactionQueue, DirectExchange transactionExchange) {
        return BindingBuilder.bind(transactionQueue)
                .to(transactionExchange)
                .with(TRANSACTION_ROUTING_KEY);
    }
    
    /**
     * 支付通知队列配置
     */
    @Bean
    public Queue paymentNotifyQueue() {
        return new Queue(PAYMENT_NOTIFY_QUEUE, true);
    }
    
    @Bean
    public DirectExchange paymentNotifyExchange() {
        return new DirectExchange(PAYMENT_NOTIFY_EXCHANGE);
    }
    
    @Bean
    public Binding paymentNotifyBinding(Queue paymentNotifyQueue, DirectExchange paymentNotifyExchange) {
        return BindingBuilder.bind(paymentNotifyQueue)
                .to(paymentNotifyExchange)
                .with(PAYMENT_NOTIFY_ROUTING_KEY);
    }
    
    /**
     * 区块链交易队列配置
     */
    @Bean
    public Queue blockchainTxQueue() {
        return new Queue(BLOCKCHAIN_TX_QUEUE, true);
    }
    
    @Bean
    public DirectExchange blockchainTxExchange() {
        return new DirectExchange(BLOCKCHAIN_TX_EXCHANGE);
    }
    
    @Bean
    public Binding blockchainTxBinding(Queue blockchainTxQueue, DirectExchange blockchainTxExchange) {
        return BindingBuilder.bind(blockchainTxQueue)
                .to(blockchainTxExchange)
                .with(BLOCKCHAIN_TX_ROUTING_KEY);
    }
    
    /**
     * JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        
        // 配置确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("✅ 消息发送成功: " + correlationData);
            } else {
                System.err.println("❌ 消息发送失败: " + cause);
            }
        });
        
        // 配置返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("❌ 消息路由失败: " + returned.getMessage() + 
                    " - Exchange: " + returned.getExchange() + 
                    " - RoutingKey: " + returned.getRoutingKey());
        });
        
        return rabbitTemplate;
    }
}