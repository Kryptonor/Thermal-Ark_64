package com.thermalark.repository;

import com.thermalark.entity.EnergyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<EnergyOrder, Long> {
    List<EnergyOrder> findByUserId(Long userId);
    List<EnergyOrder> findByStatus(EnergyOrder.OrderStatus status);
    
    @Query("SELECT o FROM EnergyOrder o WHERE o.type = :type AND o.status = 'PENDING' AND o.price <= :price ORDER BY o.price ASC, o.createdAt ASC")
    List<EnergyOrder> findMatchingBuyOrders(@Param("type") EnergyOrder.OrderType type, @Param("price") BigDecimal price);
    
    @Query("SELECT o FROM EnergyOrder o WHERE o.type = :type AND o.status = 'PENDING' AND o.price >= :price ORDER BY o.price DESC, o.createdAt ASC")
    List<EnergyOrder> findMatchingSellOrders(@Param("type") EnergyOrder.OrderType type, @Param("price") BigDecimal price);
    
    @Query("SELECT COUNT(o) FROM EnergyOrder o WHERE o.status = :status")
    Long countByStatus(@Param("status") EnergyOrder.OrderStatus status);
    
    @Query("SELECT SUM(o.energyAmount) FROM EnergyOrder o WHERE o.status = 'COMPLETED' AND o.type = 'SELL'")
    BigDecimal getTotalTradedEnergy();
}