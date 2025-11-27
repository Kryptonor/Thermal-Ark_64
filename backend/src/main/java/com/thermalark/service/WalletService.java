package com.thermalark.service;

import com.thermalark.entity.User;
import com.thermalark.entity.Wallet;
import com.thermalark.repository.UserRepository;
import com.thermalark.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final BlockchainService blockchainService;
    
    /**
     * 创建用户钱包
     */
    @Transactional
    public Wallet createWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否已有钱包
        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);
        if (existingWallet.isPresent()) {
            throw new RuntimeException("用户已存在钱包");
        }
        
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setBlockchainBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        
        return walletRepository.save(wallet);
    }
    
    /**
     * 获取用户钱包余额
     */
    public WalletBalance getWalletBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("钱包不存在"));
        
        WalletBalance balance = new WalletBalance();
        balance.setAvailableBalance(wallet.getBalance());
        balance.setFrozenBalance(wallet.getFrozenBalance());
        balance.setBlockchainBalance(wallet.getBlockchainBalance());
        balance.setTotalBalance(wallet.getBalance().add(wallet.getBlockchainBalance()));
        
        return balance;
    }
    
    /**
     * 充值
     */
    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount, String paymentMethod) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("钱包不存在"));
        
        // 模拟支付处理
        boolean paymentSuccess = processPayment(amount, paymentMethod);
        if (!paymentSuccess) {
            throw new RuntimeException("支付处理失败");
        }
        
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        
        return walletRepository.save(wallet);
    }
    
    /**
     * 提现
     */
    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount, String withdrawMethod) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("提现金额必须大于0");
        }
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("钱包不存在"));
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }
        
        // 模拟提现处理
        boolean withdrawSuccess = processWithdrawal(amount, withdrawMethod);
        if (!withdrawSuccess) {
            throw new RuntimeException("提现处理失败");
        }
        
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        
        return walletRepository.save(wallet);
    }
    
    /**
     * 区块链余额同步
     */
    @Transactional
    public Wallet syncBlockchainBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getBlockchainAddress() == null) {
            throw new RuntimeException("用户未绑定区块链地址");
        }
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("钱包不存在"));
        
        try {
            BigDecimal blockchainBalance = blockchainService.getBalance(user.getBlockchainAddress());
            wallet.setBlockchainBalance(blockchainBalance);
            wallet.setUpdatedAt(LocalDateTime.now());
            
            return walletRepository.save(wallet);
        } catch (Exception e) {
            throw new RuntimeException("区块链余额同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 资金转移（系统内部）
     */
    @Transactional
    public void transferFunds(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("转账金额必须大于0");
        }
        
        Wallet fromWallet = walletRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new RuntimeException("转出方钱包不存在"));
        Wallet toWallet = walletRepository.findByUserId(toUserId)
                .orElseThrow(() -> new RuntimeException("转入方钱包不存在"));
        
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("转出方余额不足");
        }
        
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
    }
    
    /**
     * 获取系统总余额统计
     */
    public SystemBalanceStats getSystemBalanceStats() {
        SystemBalanceStats stats = new SystemBalanceStats();
        stats.setTotalSystemBalance(walletRepository.getTotalSystemBalance() != null ? 
            walletRepository.getTotalSystemBalance() : BigDecimal.ZERO);
        stats.setTotalFrozenBalance(walletRepository.getTotalFrozenBalance() != null ? 
            walletRepository.getTotalFrozenBalance() : BigDecimal.ZERO);
        return stats;
    }
    
    /**
     * 模拟支付处理
     */
    private boolean processPayment(BigDecimal amount, String paymentMethod) {
        // 这里应该集成实际的支付网关
        // 目前模拟支付成功
        return true;
    }
    
    /**
     * 模拟提现处理
     */
    private boolean processWithdrawal(BigDecimal amount, String withdrawMethod) {
        // 这里应该集成实际的提现渠道
        // 目前模拟提现成功
        return true;
    }
    
    public static class WalletBalance {
        private BigDecimal availableBalance;
        private BigDecimal frozenBalance;
        private BigDecimal blockchainBalance;
        private BigDecimal totalBalance;
        
        // getters and setters
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
        public BigDecimal getBlockchainBalance() { return blockchainBalance; }
        public void setBlockchainBalance(BigDecimal blockchainBalance) { this.blockchainBalance = blockchainBalance; }
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
    }
    
    public static class SystemBalanceStats {
        private BigDecimal totalSystemBalance;
        private BigDecimal totalFrozenBalance;
        
        // getters and setters
        public BigDecimal getTotalSystemBalance() { return totalSystemBalance; }
        public void setTotalSystemBalance(BigDecimal totalSystemBalance) { this.totalSystemBalance = totalSystemBalance; }
        public BigDecimal getTotalFrozenBalance() { return totalFrozenBalance; }
        public void setTotalFrozenBalance(BigDecimal totalFrozenBalance) { this.totalFrozenBalance = totalFrozenBalance; }
    }
}