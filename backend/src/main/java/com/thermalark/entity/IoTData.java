package com.thermalark.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "iot_data")
@Data
public class IoTData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column(nullable = false)
    private BigDecimal temperature;
    
    @Column(nullable = false)
    private BigDecimal energyOutput;
    
    @Column(nullable = false)
    private BigDecimal energyConsumption;
    
    @Column(nullable = false)
    private BigDecimal efficiency;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private Boolean isActive = true;
}