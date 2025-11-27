package com.thermalark.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "blockchain")
public class BlockchainConfig {
    
    /**
     * FISCO BCOS节点URL
     */
    private String nodeUrl = "http://localhost:8545";
    
    /**
     * 链ID
     */
    private Long chainId = 1L;
    
    /**
     * 群组ID
     */
    private Integer groupId = 1;
    
    /**
     * 私钥（用于交易签名）
     */
    private String privateKey = "0x...";
    
    /**
     * 智能合约地址
     */
    private String heatTradingContractAddress = "0x...";
    
    /**
     * 智能合约ABI文件路径
     */
    private String contractAbiPath = "/contracts/HeatTrading.json";
    
    /**
     * 交易超时时间（秒）
     */
    private Integer transactionTimeout = 60;
    
    /**
     * 是否启用区块链
     */
    private Boolean enabled = true;
}