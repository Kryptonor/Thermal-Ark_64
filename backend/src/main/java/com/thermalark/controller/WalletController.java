package com.thermalark.controller;

import com.thermalark.entity.Wallet;
import com.thermalark.service.WalletService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * 获取钱包余额
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getWalletBalance(@RequestParam Long userId) {
        try {
            WalletService.WalletBalance balance = walletService.getWalletBalance(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balance", balance);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 充值
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request) {
        try {
            Wallet wallet = walletService.deposit(request.getUserId(), request.getAmount(), request.getPaymentMethod());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "充值成功");
            response.put("newBalance", wallet.getBalance());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 提现
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawRequest request) {
        try {
            Wallet wallet = walletService.withdraw(request.getUserId(), request.getAmount(), request.getWithdrawMethod());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "提现成功");
            response.put("newBalance", wallet.getBalance());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 同步区块链余额
     */
    @PostMapping("/sync-blockchain")
    public ResponseEntity<?> syncBlockchainBalance(@Valid @RequestBody SyncBlockchainRequest request) {
        try {
            Wallet wallet = walletService.syncBlockchainBalance(request.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "区块链余额同步成功");
            response.put("blockchainBalance", wallet.getBlockchainBalance());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 资金转移（系统内部）
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferRequest request) {
        try {
            walletService.transferFunds(request.getFromUserId(), request.getToUserId(), request.getAmount());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "资金转移成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取系统余额统计（管理员）
     */
    @GetMapping("/system-stats")
    public ResponseEntity<?> getSystemBalanceStats() {
        try {
            WalletService.SystemBalanceStats stats = walletService.getSystemBalanceStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统余额统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 创建钱包
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        try {
            Wallet wallet = walletService.createWallet(request.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "钱包创建成功");
            response.put("wallet", wallet);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Data
    public static class DepositRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "充值金额不能为空")
        @DecimalMin(value = "0.01", message = "充值金额必须大于0")
        private BigDecimal amount;
        
        @NotBlank(message = "支付方式不能为空")
        private String paymentMethod;
    }
    
    @Data
    public static class WithdrawRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "提现金额不能为空")
        @DecimalMin(value = "0.01", message = "提现金额必须大于0")
        private BigDecimal amount;
        
        @NotBlank(message = "提现方式不能为空")
        private String withdrawMethod;
    }
    
    @Data
    public static class SyncBlockchainRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
    }
    
    @Data
    public static class TransferRequest {
        @NotNull(message = "转出方用户ID不能为空")
        private Long fromUserId;
        
        @NotNull(message = "转入方用户ID不能为空")
        private Long toUserId;
        
        @NotNull(message = "转账金额不能为空")
        @DecimalMin(value = "0.01", message = "转账金额必须大于0")
        private BigDecimal amount;
    }
    
    @Data
    public static class CreateWalletRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
    }
}