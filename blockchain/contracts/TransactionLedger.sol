// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./ThermalToken.sol";

/**
 * @title TransactionLedger
 * @dev 交易账本合约，管理热能交易全生命周期
 */
contract TransactionLedger {
    // 交易状态枚举
    enum TransactionStatus {
        Pending,    // 待执行
        Executing,  // 执行中
        Completed,  // 已完成
        Cancelled   // 已取消
    }
    
    // 交易结构
    struct Transaction {
        uint256 transactionId;
        address seller;
        address buyer;
        uint256 energyAmount; // 热能数量（千瓦时）
        uint256 price;        // 单价（分/千瓦时）
        uint256 totalAmount;  // 总金额（分）
        TransactionStatus status;
        uint256 createdAt;
        uint256 startedAt;
        uint256 completedAt;
        uint256 cancelledAt;
        string iotDeviceId;   // IoT设备ID
        bool autoSettlement;  // 是否自动结算
    }
    
    // 合约所有者
    address public owner;
    
    // 热力积分合约引用
    ThermalToken public tokenContract;
    
    // 事件定义
    event TransactionCreated(
        uint256 indexed transactionId,
        address indexed seller,
        address indexed buyer,
        uint256 energyAmount,
        uint256 price,
        uint256 totalAmount,
        string iotDeviceId
    );
    
    event TransactionStarted(uint256 indexed transactionId, address indexed operator);
    event TransactionCompleted(uint256 indexed transactionId, address indexed operator);
    event TransactionCancelled(uint256 indexed transactionId, address indexed operator, string reason);
    event SettlementExecuted(uint256 indexed transactionId, address indexed from, address indexed to, uint256 amount);
    
    // 交易映射
    mapping(uint256 => Transaction) public transactions;
    mapping(address => uint256[]) public userTransactions;
    mapping(string => uint256[]) public deviceTransactions;
    
    // 交易计数器
    uint256 private transactionCounter;
    
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
    
    modifier validTransaction(uint256 transactionId) {
        require(transactions[transactionId].transactionId != 0, "Transaction does not exist");
        _;
    }
    
    constructor(address tokenContractAddress) {
        owner = msg.sender;
        tokenContract = ThermalToken(tokenContractAddress);
        transactionCounter = 1;
        operators[msg.sender] = true;
    }
    
    /**
     * @dev 创建交易
     * @param seller 卖方地址
     * @param buyer 买方地址
     * @param energyAmount 热能数量（千瓦时）
     * @param price 单价（分/千瓦时）
     * @param iotDeviceId IoT设备ID
     * @param autoSettlement 是否自动结算
     */
    function createTransaction(
        address seller,
        address buyer,
        uint256 energyAmount,
        uint256 price,
        string memory iotDeviceId,
        bool autoSettlement
    ) external onlyOperator returns (uint256) {
        require(seller != address(0), "Invalid seller address");
        require(buyer != address(0), "Invalid buyer address");
        require(energyAmount > 0, "Energy amount must be positive");
        require(price > 0, "Price must be positive");
        require(bytes(iotDeviceId).length > 0, "IoT device ID cannot be empty");
        
        uint256 transactionId = transactionCounter++;
        uint256 totalAmount = energyAmount * price;
        
        Transaction memory newTransaction = Transaction({
            transactionId: transactionId,
            seller: seller,
            buyer: buyer,
            energyAmount: energyAmount,
            price: price,
            totalAmount: totalAmount,
            status: TransactionStatus.Pending,
            createdAt: block.timestamp,
            startedAt: 0,
            completedAt: 0,
            cancelledAt: 0,
            iotDeviceId: iotDeviceId,
            autoSettlement: autoSettlement
        });
        
        transactions[transactionId] = newTransaction;
        userTransactions[seller].push(transactionId);
        userTransactions[buyer].push(transactionId);
        deviceTransactions[iotDeviceId].push(transactionId);
        
        emit TransactionCreated(
            transactionId,
            seller,
            buyer,
            energyAmount,
            price,
            totalAmount,
            iotDeviceId
        );
        
        return transactionId;
    }
    
    /**
     * @dev 开始执行交易
     * @param transactionId 交易ID
     */
    function startTransaction(uint256 transactionId) external onlyOperator validTransaction(transactionId) {
        Transaction storage transaction = transactions[transactionId];
        require(transaction.status == TransactionStatus.Pending, "Transaction not in pending status");
        
        transaction.status = TransactionStatus.Executing;
        transaction.startedAt = block.timestamp;
        
        emit TransactionStarted(transactionId, msg.sender);
    }
    
    /**
     * @dev 完成交易
     * @param transactionId 交易ID
     */
    function completeTransaction(uint256 transactionId) external onlyOperator validTransaction(transactionId) {
        Transaction storage transaction = transactions[transactionId];
        require(transaction.status == TransactionStatus.Executing, "Transaction not in executing status");
        
        transaction.status = TransactionStatus.Completed;
        transaction.completedAt = block.timestamp;
        
        // 如果启用自动结算，执行结算
        if (transaction.autoSettlement) {
            _executeSettlement(transaction);
        }
        
        emit TransactionCompleted(transactionId, msg.sender);
    }
    
    /**
     * @dev 取消交易
     * @param transactionId 交易ID
     * @param reason 取消原因
     */
    function cancelTransaction(uint256 transactionId, string memory reason) external onlyOperator validTransaction(transactionId) {
        Transaction storage transaction = transactions[transactionId];
        require(
            transaction.status == TransactionStatus.Pending || 
            transaction.status == TransactionStatus.Executing,
            "Transaction cannot be cancelled"
        );
        
        transaction.status = TransactionStatus.Cancelled;
        transaction.cancelledAt = block.timestamp;
        
        emit TransactionCancelled(transactionId, msg.sender, reason);
    }
    
    /**
     * @dev 执行结算
     * @param transaction 交易信息
     */
    function _executeSettlement(Transaction storage transaction) internal {
        require(tokenContract.transferFrom(transaction.buyer, transaction.seller, transaction.totalAmount), "Settlement failed");
        
        emit SettlementExecuted(
            transaction.transactionId,
            transaction.buyer,
            transaction.seller,
            transaction.totalAmount
        );
    }
    
    /**
     * @dev 手动执行结算
     * @param transactionId 交易ID
     */
    function executeSettlement(uint256 transactionId) external onlyOperator validTransaction(transactionId) {
        Transaction storage transaction = transactions[transactionId];
        require(transaction.status == TransactionStatus.Completed, "Transaction not completed");
        require(!transaction.autoSettlement, "Auto settlement enabled");
        
        _executeSettlement(transaction);
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
     * @dev 获取交易详情
     * @param transactionId 交易ID
     */
    function getTransaction(uint256 transactionId) external view validTransaction(transactionId) returns (
        uint256,
        address,
        address,
        uint256,
        uint256,
        uint256,
        TransactionStatus,
        uint256,
        uint256,
        uint256,
        uint256,
        string memory,
        bool
    ) {
        Transaction memory transaction = transactions[transactionId];
        return (
            transaction.transactionId,
            transaction.seller,
            transaction.buyer,
            transaction.energyAmount,
            transaction.price,
            transaction.totalAmount,
            transaction.status,
            transaction.createdAt,
            transaction.startedAt,
            transaction.completedAt,
            transaction.cancelledAt,
            transaction.iotDeviceId,
            transaction.autoSettlement
        );
    }
    
    /**
     * @dev 获取用户交易列表
     * @param user 用户地址
     */
    function getUserTransactions(address user) external view returns (uint256[] memory) {
        return userTransactions[user];
    }
    
    /**
     * @dev 获取设备交易列表
     * @param deviceId 设备ID
     */
    function getDeviceTransactions(string memory deviceId) external view returns (uint256[] memory) {
        return deviceTransactions[deviceId];
    }
    
    /**
     * @dev 获取交易统计信息
     */
    function getTransactionStats() external view returns (
        uint256 totalTransactions,
        uint256 pendingCount,
        uint256 executingCount,
        uint256 completedCount,
        uint256 cancelledCount
    ) {
        totalTransactions = transactionCounter - 1;
        
        for (uint256 i = 1; i < transactionCounter; i++) {
            if (transactions[i].transactionId != 0) {
                TransactionStatus status = transactions[i].status;
                if (status == TransactionStatus.Pending) pendingCount++;
                else if (status == TransactionStatus.Executing) executingCount++;
                else if (status == TransactionStatus.Completed) completedCount++;
                else if (status == TransactionStatus.Cancelled) cancelledCount++;
            }
        }
        
        return (totalTransactions, pendingCount, executingCount, completedCount, cancelledCount);
    }
}