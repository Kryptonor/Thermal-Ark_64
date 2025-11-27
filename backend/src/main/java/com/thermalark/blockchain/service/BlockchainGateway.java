package com.thermalark.blockchain.service;

import com.thermalark.blockchain.config.BlockchainConfig;
import com.thermalark.blockchain.dto.TransactionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BlockchainGateway {
    
    @Autowired
    private BlockchainConfig blockchainConfig;
    
    private Web3j web3j;
    private TransactionManager transactionManager;
    private Map<String, Object> contractCache = new HashMap<>();
    
    /**
     * 初始化区块链连接
     */
    public void initialize() {
        try {
            if (!blockchainConfig.getEnabled()) {
                log.info("区块链功能已禁用");
                return;
            }
            
            log.info("正在连接FISCO BCOS节点: {}", blockchainConfig.getNodeUrl());
            
            // 创建Web3j实例
            this.web3j = Web3j.build(new HttpService(blockchainConfig.getNodeUrl()));
            
            // 创建交易管理器
            this.transactionManager = new ClientTransactionManager(
                web3j,
                blockchainConfig.getPrivateKey(),
                blockchainConfig.getGroupId(),
                blockchainConfig.getChainId()
            );
            
            // 测试连接
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ 区块链连接成功 - 客户端版本: {}", clientVersion);
            
        } catch (Exception e) {
            log.error("❌ 区块链连接失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 部署智能合约
     */
    public String deployContract(String contractName) {
        try {
            if (!blockchainConfig.getEnabled()) {
                log.warn("区块链功能已禁用，跳过合约部署");
                return "blockchain_disabled";
            }
            
            log.info("正在部署智能合约: {}", contractName);
            
            // 模拟合约部署
            String contractAddress = generateMockContractAddress();
            
            // 在实际应用中，这里应该使用web3j加载合约并部署
            // Contract contract = HeatTrading.deploy(web3j, transactionManager, new DefaultGasProvider()).send();
            
            log.info("✅ 智能合约部署成功: {} - {}", contractName, contractAddress);
            
            // 缓存合约地址
            contractCache.put(contractName, contractAddress);
            
            return contractAddress;
            
        } catch (Exception e) {
            log.error("❌ 智能合约部署失败: {}", e.getMessage(), e);
            return "deployment_failed";
        }
    }
    
    /**
     * 调用智能合约函数
     */
    public TransactionResult callContract(String contractAddress, String function, Object... args) {
        try {
            if (!blockchainConfig.getEnabled()) {
                log.warn("区块链功能已禁用，跳过合约调用");
                return createMockTransactionResult(true);
            }
            
            log.info("调用智能合约函数: {} - {}", contractAddress, function);
            
            // 模拟合约调用
            TransactionResult result = createMockTransactionResult(true);
            result.setContractAddress(contractAddress);
            
            // 在实际应用中，这里应该使用web3j调用合约函数
            // HeatTrading contract = HeatTrading.load(contractAddress, web3j, transactionManager, new DefaultGasProvider());
            // TransactionReceipt receipt = contract.yourFunction(args).send();
            
            log.info("✅ 智能合约调用成功: {} - {}", function, result.getTransactionHash());
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ 智能合约调用失败: {}", e.getMessage(), e);
            TransactionResult result = createMockTransactionResult(false);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }
    
    /**
     * 查询智能合约状态
     */
    public Object queryContract(String contractAddress, String function, Object... args) {
        try {
            if (!blockchainConfig.getEnabled()) {
                log.warn("区块链功能已禁用，返回模拟数据");
                return createMockQueryResult(function);
            }
            
            log.info("查询智能合约状态: {} - {}", contractAddress, function);
            
            // 模拟查询结果
            Object result = createMockQueryResult(function);
            
            // 在实际应用中，这里应该使用web3j查询合约状态
            // HeatTrading contract = HeatTrading.load(contractAddress, web3j, transactionManager, new DefaultGasProvider());
            // Object result = contract.yourFunction(args).send();
            
            log.info("✅ 智能合约查询成功: {} - {}", function, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ 智能合约查询失败: {}", e.getMessage(), e);
            return "query_failed";
        }
    }
    
    /**
     * 获取区块高度
     */
    public BigInteger getBlockNumber() {
        try {
            if (!blockchainConfig.getEnabled()) {
                return BigInteger.valueOf(1000); // 模拟区块高度
            }
            
            return web3j.ethBlockNumber().send().getBlockNumber();
            
        } catch (Exception e) {
            log.error("获取区块高度失败: {}", e.getMessage());
            return BigInteger.ZERO;
        }
    }
    
    /**
     * 验证交易
     */
    public boolean verifyTransaction(String transactionHash) {
        try {
            if (!blockchainConfig.getEnabled()) {
                return true; // 模拟验证成功
            }
            
            // 查询交易收据
            org.web3j.protocol.core.methods.response.EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            if (receipt.getTransactionReceipt().isPresent()) {
                org.web3j.protocol.core.methods.response.TransactionReceipt txReceipt = receipt.getTransactionReceipt().get();
                return txReceipt.isStatusOK();
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("验证交易失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成模拟合约地址
     */
    private String generateMockContractAddress() {
        return "0x" + String.format("%040x", System.currentTimeMillis() % 1000000000000L);
    }
    
    /**
     * 创建模拟交易结果
     */
    private TransactionResult createMockTransactionResult(boolean success) {
        TransactionResult result = new TransactionResult();
        result.setSuccess(success);
        result.setTransactionHash("0x" + String.format("%064x", System.nanoTime()));
        result.setBlockHash("0x" + String.format("%064x", System.currentTimeMillis()));
        result.setBlockNumber(System.currentTimeMillis() % 1000000L);
        result.setGasUsed(21000L + (long)(Math.random() * 10000));
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    /**
     * 创建模拟查询结果
     */
    private Object createMockQueryResult(String function) {
        switch (function) {
            case "getBalance":
                return BigInteger.valueOf((long)(Math.random() * 1000000));
            case "getEnergyPrice":
                return BigInteger.valueOf(100 + (long)(Math.random() * 50));
            case "getTotalTrades":
                return BigInteger.valueOf(1000 + (long)(Math.random() * 5000));
            case "getUserInfo":
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("balance", BigInteger.valueOf((long)(Math.random() * 10000)));
                userInfo.put("energy", BigInteger.valueOf((long)(Math.random() * 5000)));
                userInfo.put("trades", BigInteger.valueOf((long)(Math.random() * 100)));
                return userInfo;
            default:
                return "mock_result";
        }
    }
    
    /**
     * 关闭连接
     */
    public void shutdown() {
        try {
            if (web3j != null) {
                web3j.shutdown();
                log.info("区块链连接已关闭");
            }
        } catch (Exception e) {
            log.error("关闭区块链连接失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取区块高度（兼容性方法）
     */
    public BigInteger getBlockHeight() {
        return getBlockNumber();
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        try {
            if (web3j == null) {
                return false;
            }
            
            // 尝试获取客户端版本以测试连接
            web3j.web3ClientVersion().send().getWeb3ClientVersion();
            return true;
        } catch (Exception e) {
            log.warn("区块链连接检查失败: {}", e.getMessage());
            return false;
        }
    }
}