package com.thermalark.service;

import com.thermalark.entity.EnergyOrder;
import com.thermalark.entity.Transaction;
import com.thermalark.entity.User;
import com.thermalark.entity.Wallet;
import com.thermalark.repository.OrderRepository;
import com.thermalark.repository.TransactionRepository;
import com.thermalark.repository.UserRepository;
import com.thermalark.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradingService {
    
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BlockchainService blockchainService;
    
    /**
     * 创建能源订单
     */
    @Transactional
    public EnergyOrder createOrder(Long userId, EnergyOrder.OrderType type, 
                                  BigDecimal energyAmount, BigDecimal price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证用户是否已验证
        if (!user.getIsVerified()) {
            throw new RuntimeException("用户未完成身份验证");
        }
        
        // 获取用户钱包
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户钱包不存在"));
        
        BigDecimal totalAmount = energyAmount.multiply(price).setScale(2, RoundingMode.HALF_UP);
        
        // 如果是买入订单，检查余额是否足够
        if (type == EnergyOrder.OrderType.BUY) {
            if (wallet.getBalance().compareTo(totalAmount) < 0) {
                throw new RuntimeException("余额不足");
            }
            // 冻结资金
            wallet.setFrozenBalance(wallet.getFrozenBalance().add(totalAmount));
            wallet.setBalance(wallet.getBalance().subtract(totalAmount));
        }
        
        // 创建订单
        EnergyOrder order = new EnergyOrder();
        order.setUser(user);
        order.setType(type);
        order.setEnergyAmount(energyAmount);
        order.setPrice(price);
        order.setTotalAmount(totalAmount);
        order.setStatus(EnergyOrder.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        EnergyOrder savedOrder = orderRepository.save(order);
        walletRepository.save(wallet);
        
        // 尝试撮合订单
        matchOrders();
        
        return savedOrder;
    }
    
    /**
     * 订单撮合
     */
    @Transactional
    public void matchOrders() {
        // 获取所有待处理的卖出订单
        List<EnergyOrder> sellOrders = orderRepository.findByStatus(EnergyOrder.OrderStatus.PENDING);
        
        for (EnergyOrder sellOrder : sellOrders) {
            if (sellOrder.getType() == EnergyOrder.OrderType.SELL) {
                // 寻找匹配的买入订单
                List<EnergyOrder> buyOrders = orderRepository.findMatchingBuyOrders(
                    EnergyOrder.OrderType.BUY, sellOrder.getPrice());
                
                for (EnergyOrder buyOrder : buyOrders) {
                    if (sellOrder.getEnergyAmount().compareTo(buyOrder.getEnergyAmount()) <= 0) {
                        // 创建交易
                        createTransaction(sellOrder, buyOrder);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * 创建交易
     */
    @Transactional
    private void createTransaction(EnergyOrder sellOrder, EnergyOrder buyOrder) {
        Transaction transaction = new Transaction();
        transaction.setSeller(sellOrder.getUser());
        transaction.setBuyer(buyOrder.getUser());
        transaction.setEnergyAmount(sellOrder.getEnergyAmount());
        transaction.setPrice(sellOrder.getPrice());
        transaction.setTotalAmount(sellOrder.getTotalAmount());
        transaction.setStatus(Transaction.TransactionStatus.CREATED);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // 更新订单状态
        sellOrder.setStatus(EnergyOrder.OrderStatus.MATCHED);
        buyOrder.setStatus(EnergyOrder.OrderStatus.MATCHED);
        
        // 如果是部分成交，创建新的买入订单
        if (sellOrder.getEnergyAmount().compareTo(buyOrder.getEnergyAmount()) < 0) {
            BigDecimal remainingAmount = buyOrder.getEnergyAmount().subtract(sellOrder.getEnergyAmount());
            BigDecimal remainingTotal = remainingAmount.multiply(buyOrder.getPrice());
            
            EnergyOrder remainingOrder = new EnergyOrder();
            remainingOrder.setUser(buyOrder.getUser());
            remainingOrder.setType(EnergyOrder.OrderType.BUY);
            remainingOrder.setEnergyAmount(remainingAmount);
            remainingOrder.setPrice(buyOrder.getPrice());
            remainingOrder.setTotalAmount(remainingTotal);
            remainingOrder.setStatus(EnergyOrder.OrderStatus.PENDING);
            remainingOrder.setCreatedAt(LocalDateTime.now());
            
            orderRepository.save(remainingOrder);
        }
        
        transactionRepository.save(transaction);
        orderRepository.save(sellOrder);
        orderRepository.save(buyOrder);
        
        // 执行交易
        executeTransaction(transaction.getId());
    }
    
    /**
     * 执行交易
     */
    @Transactional
    public void executeTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("交易不存在"));
        
        // 更新交易状态
        transaction.setStatus(Transaction.TransactionStatus.EXECUTING);
        transaction.setStartedAt(LocalDateTime.now());
        
        // 获取买卖双方钱包
        Wallet sellerWallet = walletRepository.findByUserId(transaction.getSeller().getId())
                .orElseThrow(() -> new RuntimeException("卖方钱包不存在"));
        Wallet buyerWallet = walletRepository.findByUserId(transaction.getBuyer().getId())
                .orElseThrow(() -> new RuntimeException("买方钱包不存在"));
        
        // 资金转移
        sellerWallet.setBalance(sellerWallet.getBalance().add(transaction.getTotalAmount()));
        buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().subtract(transaction.getTotalAmount()));
        
        // 区块链上链
        try {
            Long blockchainTxId = blockchainService.recordTransaction(
                transaction.getSeller().getBlockchainAddress(),
                transaction.getBuyer().getBlockchainAddress(),
                transaction.getEnergyAmount(),
                transaction.getTotalAmount()
            );
            transaction.setBlockchainTransactionId(blockchainTxId);
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            // 回滚资金转移
            sellerWallet.setBalance(sellerWallet.getBalance().subtract(transaction.getTotalAmount()));
            buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().add(transaction.getTotalAmount()));
        }
        
        transactionRepository.save(transaction);
        walletRepository.save(sellerWallet);
        walletRepository.save(buyerWallet);
    }
    
    /**
     * 获取用户订单列表
     */
    public List<EnergyOrder> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        EnergyOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (order.getStatus() != EnergyOrder.OrderStatus.PENDING) {
            throw new RuntimeException("只能取消待处理的订单");
        }
        
        // 如果是买入订单，解冻资金
        if (order.getType() == EnergyOrder.OrderType.BUY) {
            Wallet wallet = walletRepository.findByUserId(order.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("用户钱包不存在"));
            wallet.setBalance(wallet.getBalance().add(order.getTotalAmount()));
            wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(order.getTotalAmount()));
            walletRepository.save(wallet);
        }
        
        order.setStatus(EnergyOrder.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
    
    /**
     * 获取交易统计
     */
    public TradingStats getTradingStats() {
        TradingStats stats = new TradingStats();
        stats.setTotalTransactions(transactionRepository.countByStatus(Transaction.TransactionStatus.COMPLETED));
        stats.setTotalTradedEnergy(transactionRepository.getTotalTradedEnergy() != null ? 
            transactionRepository.getTotalTradedEnergy() : BigDecimal.ZERO);
        stats.setTotalTransactionAmount(transactionRepository.getTotalTransactionAmount() != null ? 
            transactionRepository.getTotalTransactionAmount() : BigDecimal.ZERO);
        return stats;
    }
    
    public static class TradingStats {
        private Long totalTransactions;
        private BigDecimal totalTradedEnergy;
        private BigDecimal totalTransactionAmount;
        
        // getters and setters
        public Long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }
        public BigDecimal getTotalTradedEnergy() { return totalTradedEnergy; }
        public void setTotalTradedEnergy(BigDecimal totalTradedEnergy) { this.totalTradedEnergy = totalTradedEnergy; }
        public BigDecimal getTotalTransactionAmount() { return totalTransactionAmount; }
        public void setTotalTransactionAmount(BigDecimal totalTransactionAmount) { this.totalTransactionAmount = totalTransactionAmount; }
    }
}