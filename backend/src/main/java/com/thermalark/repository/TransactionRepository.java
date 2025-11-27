package com.thermalark.repository;

import com.thermalark.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerId(Long buyerId);
    List<Transaction> findBySellerId(Long sellerId);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.buyer.id = :userId OR t.seller.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") Transaction.TransactionStatus status);
    
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalTransactionAmount();
    
    @Query("SELECT SUM(t.energyAmount) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalTradedEnergy();
    
    Optional<Transaction> findByBlockchainTransactionId(Long blockchainTransactionId);
}