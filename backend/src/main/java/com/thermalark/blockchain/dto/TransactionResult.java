package com.thermalark.blockchain.dto;

import lombok.Data;

@Data
public class TransactionResult {
    private boolean success;
    private String transactionHash;
    private String blockHash;
    private Long blockNumber;
    private String contractAddress;
    private String errorMessage;
    private Long gasUsed;
    private Long timestamp;
    
    /**
     * 获取错误消息（兼容性方法）
     */
    public String getMessage() {
        return errorMessage;
    }
    
    /**
     * 设置错误消息（兼容性方法）
     */
    public void setMessage(String message) {
        this.errorMessage = message;
    }
}