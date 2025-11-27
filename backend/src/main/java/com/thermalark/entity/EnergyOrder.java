package com.thermalark.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_orders")
@Data
public class EnergyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType type;
    
    @Column(nullable = false)
    private BigDecimal energyAmount;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    private String iotDeviceId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime matchedAt;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime cancelledAt;
    
    private LocalDateTime updatedAt;
    
    private String blockchainTxHash;
    
    public enum OrderType {
        BUY, SELL
    }
    
    public enum OrderStatus {
        PENDING, MATCHED, EXECUTING, COMPLETED, CANCELLED
    }
}