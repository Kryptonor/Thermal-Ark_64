// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./TransactionLedger.sol";

/**
 * @title AutoSettlement
 * @dev 自动结算合约，监听交易完成事件并执行结算
 */
contract AutoSettlement {
    // 结算配置结构
    struct SettlementConfig {
        uint256 feeRate;           // 手续费率（万分之几）
        address feeReceiver;       // 手续费接收地址
        bool enabled;              // 是否启用自动结算
        uint256 minSettlementAmount; // 最小结算金额（分）
    }
    
    // 合约所有者
    address public owner;
    
    // 交易账本合约引用
    TransactionLedger public ledgerContract;
    
    // 事件定义
    event SettlementConfigUpdated(uint256 feeRate, address feeReceiver, bool enabled, uint256 minAmount);
    event AutoSettlementExecuted(uint256 indexed transactionId, uint256 feeAmount, uint256 netAmount);
    event ManualSettlementExecuted(uint256 indexed transactionId, address indexed operator);
    
    // 结算配置
    SettlementConfig public config;
    
    // 操作员映射
    mapping(address => bool) public operators;
    
    // 修饰器
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }
    
    modifier onlyOperator() {
        require(operators[msg.sender] || msg.sender == owner, "Only operator can call this function");
        _;
    }
    
    constructor(address ledgerContractAddress) {
        owner = msg.sender;
        ledgerContract = TransactionLedger(ledgerContractAddress);
        
        // 默认配置
        config = SettlementConfig({
            feeRate: 50, // 0.5%
            feeReceiver: msg.sender,
            enabled: true,
            minSettlementAmount: 100 // 1元
        });
        
        operators[msg.sender] = true;
    }
    
    /**
     * @dev 监听交易完成事件并执行自动结算
     * @param transactionId 交易ID
     */
    function onTransactionCompleted(uint256 transactionId) external onlyOperator {
        require(config.enabled, "Auto settlement is disabled");
        
        // 获取交易详情
        (
            uint256 id,
            address seller,
            address buyer,
            uint256 energyAmount,
            uint256 price,
            uint256 totalAmount,
            TransactionLedger.TransactionStatus status,
            ,,,,,
            bool autoSettlement
        ) = ledgerContract.getTransaction(transactionId);
        
        require(id == transactionId, "Transaction not found");
        require(status == TransactionLedger.TransactionStatus.Completed, "Transaction not completed");
        require(autoSettlement, "Auto settlement not enabled for this transaction");
        require(totalAmount >= config.minSettlementAmount, "Amount below minimum settlement threshold");
        
        // 计算手续费和净额
        uint256 feeAmount = (totalAmount * config.feeRate) / 10000;
        uint256 netAmount = totalAmount - feeAmount;
        
        // 执行结算（这里需要调用热力积分合约的转账功能）
        // 实际实现中需要集成ThermalToken合约
        
        emit AutoSettlementExecuted(transactionId, feeAmount, netAmount);
    }
    
    /**
     * @dev 手动执行结算
     * @param transactionId 交易ID
     */
    function executeManualSettlement(uint256 transactionId) external onlyOperator {
        // 获取交易详情
        (
            uint256 id,
            address seller,
            address buyer,
            uint256 energyAmount,
            uint256 price,
            uint256 totalAmount,
            TransactionLedger.TransactionStatus status,
            ,,,,,
            bool autoSettlement
        ) = ledgerContract.getTransaction(transactionId);
        
        require(id == transactionId, "Transaction not found");
        require(status == TransactionLedger.TransactionStatus.Completed, "Transaction not completed");
        require(!autoSettlement, "Auto settlement enabled, use auto mode");
        
        // 计算手续费和净额
        uint256 feeAmount = (totalAmount * config.feeRate) / 10000;
        uint256 netAmount = totalAmount - feeAmount;
        
        // 执行结算（这里需要调用热力积分合约的转账功能）
        // 实际实现中需要集成ThermalToken合约
        
        emit ManualSettlementExecuted(transactionId, msg.sender);
        emit AutoSettlementExecuted(transactionId, feeAmount, netAmount);
    }
    
    /**
     * @dev 更新结算配置
     * @param feeRate 手续费率（万分之几）
     * @param feeReceiver 手续费接收地址
     * @param enabled 是否启用自动结算
     * @param minSettlementAmount 最小结算金额（分）
     */
    function updateSettlementConfig(
        uint256 feeRate,
        address feeReceiver,
        bool enabled,
        uint256 minSettlementAmount
    ) external onlyOwner {
        require(feeRate <= 1000, "Fee rate too high"); // 最大10%
        require(feeReceiver != address(0), "Invalid fee receiver");
        require(minSettlementAmount >= 1, "Minimum amount too low");
        
        config.feeRate = feeRate;
        config.feeReceiver = feeReceiver;
        config.enabled = enabled;
        config.minSettlementAmount = minSettlementAmount;
        
        emit SettlementConfigUpdated(feeRate, feeReceiver, enabled, minSettlementAmount);
    }
    
    /**
     * @dev 添加操作员
     * @param operator 操作员地址
     */
    function addOperator(address operator) external onlyOwner {
        operators[operator] = true;
    }
    
    /**
     * @dev 移除操作员
     * @param operator 操作员地址
     */
    function removeOperator(address operator) external onlyOwner {
        operators[operator] = false;
    }
    
    /**
     * @dev 计算结算金额
     * @param totalAmount 总金额（分）
     * @return feeAmount 手续费（分）
     * @return netAmount 净额（分）
     */
    function calculateSettlement(uint256 totalAmount) external view returns (uint256 feeAmount, uint256 netAmount) {
        feeAmount = (totalAmount * config.feeRate) / 10000;
        netAmount = totalAmount - feeAmount;
        return (feeAmount, netAmount);
    }
    
    /**
     * @dev 获取结算配置
     */
    function getSettlementConfig() external view returns (
        uint256 feeRate,
        address feeReceiver,
        bool enabled,
        uint256 minSettlementAmount
    ) {
        return (
            config.feeRate,
            config.feeReceiver,
            config.enabled,
            config.minSettlementAmount
        );
    }
    
    /**
     * @dev 检查交易是否满足自动结算条件
     * @param transactionId 交易ID
     */
    function canAutoSettle(uint256 transactionId) external view returns (bool) {
        if (!config.enabled) return false;
        
        try ledgerContract.getTransaction(transactionId) returns (
            uint256 id,
            ,,
            ,,
            uint256 totalAmount,
            TransactionLedger.TransactionStatus status,
            ,,,,,
            bool autoSettlement
        ) {
            return (
                id == transactionId &&
                status == TransactionLedger.TransactionStatus.Completed &&
                autoSettlement &&
                totalAmount >= config.minSettlementAmount
            );
        } catch {
            return false;
        }
    }
}