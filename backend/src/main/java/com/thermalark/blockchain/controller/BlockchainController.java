package com.thermalark.blockchain.controller;

import com.thermalark.blockchain.dto.TransactionResult;
import com.thermalark.blockchain.service.BlockchainGateway;
import com.thermalark.mq.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@Slf4j
public class BlockchainController {
    
    @Autowired
    private BlockchainGateway blockchainGateway;
    
    @Autowired
    private MessageQueueService messageQueueService;
    
    /**
     * 部署智能合约
     */
    @PostMapping("/contract/deploy")
    public ResponseEntity<Map<String, Object>> deployContract(@RequestParam String contractName) {
        try {
            log.info("🔗 部署智能合约: contractName={}", contractName);
            
            String contractAddress = blockchainGateway.deployContract(contractName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contractAddress", contractAddress);
            response.put("contractName", contractName);
            response.put("message", "智能合约部署成功");
            response.put("timestamp", System.currentTimeMillis());
            
            // 发送区块链交易消息
            Map<String, Object> blockchainData = messageQueueService.buildBlockchainTransactionMessage(
                "deploy_" + System.currentTimeMillis(),
                contractAddress,
                "deploy",
                new Object[]{contractName}
            );
            messageQueueService.sendBlockchainTransaction(blockchainData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 智能合约部署失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "智能合约部署失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 调用智能合约函数
     */
    @PostMapping("/contract/{contractAddress}/call")
    public ResponseEntity<Map<String, Object>> callContract(
            @PathVariable String contractAddress,
            @RequestParam String function,
            @RequestBody(required = false) Object[] args) {
        try {
            log.info("🔗 调用智能合约函数: contractAddress={}, function={}", contractAddress, function);
            
            TransactionResult result = blockchainGateway.callContract(contractAddress, function, args);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("transactionHash", result.getTransactionHash());
            response.put("contractAddress", contractAddress);
            response.put("function", function);
            response.put("message", result.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            // 发送区块链交易消息
            Map<String, Object> blockchainData = messageQueueService.buildBlockchainTransactionMessage(
                result.getTransactionHash(),
                contractAddress,
                function,
                args
            );
            messageQueueService.sendBlockchainTransaction(blockchainData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 智能合约调用失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "智能合约调用失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 查询智能合约状态
     */
    @GetMapping("/contract/{contractAddress}/query")
    public ResponseEntity<Map<String, Object>> queryContract(
            @PathVariable String contractAddress,
            @RequestParam String function,
            @RequestParam(required = false) Object[] args) {
        try {
            log.info("🔗 查询智能合约状态: contractAddress={}, function={}", contractAddress, function);
            
            Object result = blockchainGateway.queryContract(contractAddress, function, args);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contractAddress", contractAddress);
            response.put("function", function);
            response.put("result", result);
            response.put("message", "查询成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 智能合约查询失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "智能合约查询失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取区块链状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBlockchainStatus() {
        try {
            log.info("🔗 查询区块链状态");
            
            long blockHeight = blockchainGateway.getBlockHeight();
            boolean isConnected = blockchainGateway.isConnected();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connected", isConnected);
            response.put("blockHeight", blockHeight);
            response.put("network", "FISCO BCOS");
            response.put("message", "区块链状态查询成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 区块链状态查询失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "区块链状态查询失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 记录能源交易到区块链
     */
    @PostMapping("/energy/transaction")
    public ResponseEntity<Map<String, Object>> recordEnergyTransaction(
            @RequestParam String buyerId,
            @RequestParam String sellerId,
            @RequestParam double energyAmount,
            @RequestParam double price,
            @RequestParam(required = false) String contractAddress) {
        try {
            log.info("🔗 记录能源交易到区块链: buyerId={}, sellerId={}, amount={}", 
                    buyerId, sellerId, energyAmount);
            
            String transactionId = "tx_" + System.currentTimeMillis();
            
            // 构建交易数据
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("transactionId", transactionId);
            transactionData.put("buyerId", buyerId);
            transactionData.put("sellerId", sellerId);
            transactionData.put("energyAmount", energyAmount);
            transactionData.put("price", price);
            transactionData.put("timestamp", System.currentTimeMillis());
            
            // 发送交易匹配消息
            Map<String, Object> matchData = messageQueueService.buildTransactionMatchMessage(
                transactionId, buyerId, sellerId, energyAmount, price
            );
            messageQueueService.sendTransactionMatch(matchData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionId", transactionId);
            response.put("message", "能源交易记录成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 能源交易记录失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "能源交易记录失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 验证交易
     */
    @GetMapping("/transaction/{transactionHash}/verify")
    public ResponseEntity<Map<String, Object>> verifyTransaction(@PathVariable String transactionHash) {
        try {
            log.info("🔗 验证交易: transactionHash={}", transactionHash);
            
            boolean isValid = blockchainGateway.verifyTransaction(transactionHash);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionHash", transactionHash);
            response.put("valid", isValid);
            response.put("message", isValid ? "交易验证成功" : "交易验证失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 交易验证失败: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "交易验证失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}