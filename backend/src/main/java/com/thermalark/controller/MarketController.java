package com.thermalark.controller;

import com.thermalark.entity.EnergyOrder;
import com.thermalark.service.TradingService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {
    
    private final TradingService tradingService;
    
    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam(required = false) Long userId) {
        try {
            List<EnergyOrder> orders;
            if (userId != null) {
                orders = tradingService.getUserOrders(userId);
            } else {
                // 获取所有订单（需要管理员权限）
                // 这里应该添加权限检查
                orders = tradingService.getUserOrders(null); // 临时实现
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("total", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取订单列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 创建新订单
     */
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            EnergyOrder order = tradingService.createOrder(
                request.getUserId(),
                request.getType(),
                request.getEnergyAmount(),
                request.getPrice()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 订单撮合
     */
    @PostMapping("/match")
    public ResponseEntity<?> matchOrders() {
        try {
            tradingService.matchOrders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单撮合完成");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "订单撮合失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            tradingService.cancelOrder(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单取消成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取交易统计
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTradingStats() {
        try {
            TradingService.TradingStats stats = tradingService.getTradingStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取交易统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Data
    public static class CreateOrderRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "订单类型不能为空")
        private EnergyOrder.OrderType type;
        
        @NotNull(message = "能源数量不能为空")
        @DecimalMin(value = "0.01", message = "能源数量必须大于0")
        private BigDecimal energyAmount;
        
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;
    }
}