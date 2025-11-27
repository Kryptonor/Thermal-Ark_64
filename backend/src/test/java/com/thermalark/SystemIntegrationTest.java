package com.thermalark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import com.thermalark.service.UserService;
import com.thermalark.service.TradingService;
import com.thermalark.service.PaymentService;
import com.thermalark.service.BlockchainService;
import com.thermalark.entity.User;
import com.thermalark.entity.TradingOrder;
import com.thermalark.entity.PaymentRecord;
import com.thermalark.entity.BlockchainTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class SystemIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TradingService tradingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BlockchainService blockchainService;

    @Test
    void testCompleteTradingFlow() {
        // 测试完整的交易流程
        // 用户注册 → 充值 → 挂单 → 撮合 → 结算 → 提现
        
        // 1. 用户注册
        System.out.println("=== 开始测试完整交易流程 ===");
        
        String username = "integration_test_user";
        String email = "integration@test.com";
        String password = "Test123456!";
        
        User registeredUser = userService.registerUser(username, email, password);
        assertNotNull(registeredUser, "用户注册失败");
        assertEquals(username, registeredUser.getUsername(), "用户名不匹配");
        assertEquals(email, registeredUser.getEmail(), "邮箱不匹配");
        
        System.out.println("✓ 用户注册成功: " + registeredUser.getUsername());
        
        // 2. 用户充值
        BigDecimal rechargeAmount = new BigDecimal("1000.00");
        String paymentMethod = "wechat";
        
        PaymentRecord rechargeRecord = paymentService.processPayment(
            registeredUser.getId(),
            rechargeAmount,
            paymentMethod,
            "用户充值",
            "RECHARGE"
        );
        
        assertNotNull(rechargeRecord, "充值记录创建失败");
        assertEquals("SUCCESS", rechargeRecord.getStatus(), "充值状态不正确");
        assertEquals(rechargeAmount, rechargeRecord.getAmount(), "充值金额不匹配");
        
        System.out.println("✓ 用户充值成功: " + rechargeAmount + "元");
        
        // 3. 创建买入订单
        BigDecimal buyPrice = new BigDecimal("45.50");
        BigDecimal buyQuantity = new BigDecimal("10.00");
        
        TradingOrder buyOrder = tradingService.createOrder(
            registeredUser.getId(),
            "BUY",
            buyPrice,
            buyQuantity,
            "LIMIT"
        );
        
        assertNotNull(buyOrder, "买入订单创建失败");
        assertEquals("PENDING", buyOrder.getStatus(), "订单状态不正确");
        assertEquals(buyPrice, buyOrder.getPrice(), "买入价格不匹配");
        assertEquals(buyQuantity, buyOrder.getQuantity(), "买入数量不匹配");
        
        System.out.println("✓ 买入订单创建成功: " + buyQuantity + "kWh @ " + buyPrice + "元/kWh");
        
        // 4. 创建卖出订单（模拟另一个用户）
        String sellerUsername = "integration_test_seller";
        String sellerEmail = "seller@test.com";
        User seller = userService.registerUser(sellerUsername, sellerEmail, password);
        
        // 卖家充值
        PaymentRecord sellerRecharge = paymentService.processPayment(
            seller.getId(),
            new BigDecimal("500.00"),
            "alipay",
            "卖家充值",
            "RECHARGE"
        );
        
        // 卖家创建卖出订单
        TradingOrder sellOrder = tradingService.createOrder(
            seller.getId(),
            "SELL",
            buyPrice, // 相同价格，便于撮合
            buyQuantity,
            "LIMIT"
        );
        
        assertNotNull(sellOrder, "卖出订单创建失败");
        assertEquals("PENDING", sellOrder.getStatus(), "卖出订单状态不正确");
        
        System.out.println("✓ 卖出订单创建成功: " + buyQuantity + "kWh @ " + buyPrice + "元/kWh");
        
        // 5. 订单撮合
        TradingOrder matchedOrder = tradingService.matchOrders(buyOrder.getId(), sellOrder.getId());
        assertNotNull(matchedOrder, "订单撮合失败");
        assertEquals("MATCHED", matchedOrder.getStatus(), "撮合后订单状态不正确");
        
        System.out.println("✓ 订单撮合成功");
        
        // 6. 交易结算
        boolean settlementResult = tradingService.settleTrade(matchedOrder.getId());
        assertTrue(settlementResult, "交易结算失败");
        
        // 验证订单状态更新
        TradingOrder settledBuyOrder = tradingService.getOrderById(buyOrder.getId());
        TradingOrder settledSellOrder = tradingService.getOrderById(sellOrder.getId());
        
        assertEquals("COMPLETED", settledBuyOrder.getStatus(), "买入订单结算状态不正确");
        assertEquals("COMPLETED", settledSellOrder.getStatus(), "卖出订单结算状态不正确");
        
        System.out.println("✓ 交易结算成功");
        
        // 7. 区块链交易记录
        BlockchainTransaction blockchainTx = blockchainService.recordTransaction(
            registeredUser.getId(),
            seller.getId(),
            buyQuantity,
            buyPrice,
            "ENERGY_TRADE",
            "交易完成"
        );
        
        assertNotNull(blockchainTx, "区块链交易记录失败");
        assertEquals("CONFIRMED", blockchainTx.getStatus(), "区块链交易状态不正确");
        
        System.out.println("✓ 区块链交易记录成功");
        
        // 8. 用户提现
        BigDecimal withdrawAmount = new BigDecimal("200.00");
        PaymentRecord withdrawRecord = paymentService.processPayment(
            registeredUser.getId(),
            withdrawAmount,
            "bank",
            "用户提现",
            "WITHDRAW"
        );
        
        assertNotNull(withdrawRecord, "提现记录创建失败");
        assertEquals("SUCCESS", withdrawRecord.getStatus(), "提现状态不正确");
        
        System.out.println("✓ 用户提现成功: " + withdrawAmount + "元");
        
        System.out.println("=== 完整交易流程测试完成 ===");
    }
    
    @Test
    void testUserAuthenticationFlow() {
        // 测试用户认证流程
        System.out.println("=== 开始测试用户认证流程 ===");
        
        String username = "auth_test_user";
        String email = "auth@test.com";
        String password = "Auth123456!";
        
        // 用户注册
        User user = userService.registerUser(username, email, password);
        assertNotNull(user, "用户注册失败");
        
        System.out.println("✓ 用户注册成功");
        
        // 用户登录
        boolean loginResult = userService.authenticateUser(username, password);
        assertTrue(loginResult, "用户登录失败");
        
        System.out.println("✓ 用户登录成功");
        
        // 密码重置
        String newPassword = "NewAuth123456!";
        boolean resetResult = userService.resetPassword(email, newPassword);
        assertTrue(resetResult, "密码重置失败");
        
        System.out.println("✓ 密码重置成功");
        
        // 使用新密码登录
        boolean newLoginResult = userService.authenticateUser(username, newPassword);
        assertTrue(newLoginResult, "新密码登录失败");
        
        System.out.println("✓ 新密码登录成功");
        
        System.out.println("=== 用户认证流程测试完成 ===");
    }
    
    @Test
    void testPaymentIntegration() {
        // 测试支付集成
        System.out.println("=== 开始测试支付集成 ===");
        
        String username = "payment_test_user";
        String email = "payment@test.com";
        String password = "Payment123!";
        
        User user = userService.registerUser(username, email, password);
        assertNotNull(user, "用户注册失败");
        
        // 测试微信支付
        PaymentRecord wechatPayment = paymentService.processPayment(
            user.getId(),
            new BigDecimal("100.00"),
            "wechat",
            "微信支付测试",
            "RECHARGE"
        );
        
        assertNotNull(wechatPayment, "微信支付失败");
        assertEquals("SUCCESS", wechatPayment.getStatus(), "微信支付状态不正确");
        
        System.out.println("✓ 微信支付测试成功");
        
        // 测试支付宝支付
        PaymentRecord alipayPayment = paymentService.processPayment(
            user.getId(),
            new BigDecimal("200.00"),
            "alipay",
            "支付宝支付测试",
            "RECHARGE"
        );
        
        assertNotNull(alipayPayment, "支付宝支付失败");
        assertEquals("SUCCESS", alipayPayment.getStatus(), "支付宝支付状态不正确");
        
        System.out.println("✓ 支付宝支付测试成功");
        
        // 测试银行转账
        PaymentRecord bankPayment = paymentService.processPayment(
            user.getId(),
            new BigDecimal("300.00"),
            "bank",
            "银行转账测试",
            "WITHDRAW"
        );
        
        assertNotNull(bankPayment, "银行转账失败");
        assertEquals("SUCCESS", bankPayment.getStatus(), "银行转账状态不正确");
        
        System.out.println("✓ 银行转账测试成功");
        
        System.out.println("=== 支付集成测试完成 ===");
    }
    
    @Test
    void testBlockchainIntegration() {
        // 测试区块链集成
        System.out.println("=== 开始测试区块链集成 ===");
        
        String username = "blockchain_test_user";
        String email = "blockchain@test.com";
        String password = "Blockchain123!";
        
        User user = userService.registerUser(username, email, password);
        assertNotNull(user, "用户注册失败");
        
        // 测试区块链用户注册
        boolean blockchainRegister = blockchainService.registerUserOnBlockchain(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
        
        assertTrue(blockchainRegister, "区块链用户注册失败");
        
        System.out.println("✓ 区块链用户注册成功");
        
        // 测试代币铸造
        BigDecimal tokenAmount = new BigDecimal("1000.00");
        boolean mintResult = blockchainService.mintTokens(user.getId(), tokenAmount);
        assertTrue(mintResult, "代币铸造失败");
        
        System.out.println("✓ 代币铸造成功: " + tokenAmount + " TAT");
        
        // 测试余额查询
        BigDecimal balance = blockchainService.getTokenBalance(user.getId());
        assertNotNull(balance, "余额查询失败");
        assertEquals(tokenAmount, balance, "余额不匹配");
        
        System.out.println("✓ 余额查询成功: " + balance + " TAT");
        
        // 测试交易记录
        BlockchainTransaction transaction = blockchainService.recordTransaction(
            user.getId(),
            null,
            new BigDecimal("100.00"),
            new BigDecimal("45.50"),
            "ENERGY_PRODUCTION",
            "能源生产记录"
        );
        
        assertNotNull(transaction, "交易记录失败");
        assertEquals("CONFIRMED", transaction.getStatus(), "交易状态不正确");
        
        System.out.println("✓ 区块链交易记录成功");
        
        System.out.println("=== 区块链集成测试完成 ===");
    }
}