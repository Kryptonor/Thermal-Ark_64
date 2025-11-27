package com.thermalark.repository;

import com.thermalark.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);
    
    @Query("SELECT w.balance FROM Wallet w WHERE w.user.id = :userId")
    Optional<BigDecimal> findBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w.frozenBalance FROM Wallet w WHERE w.user.id = :userId")
    Optional<BigDecimal> findFrozenBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w.blockchainBalance FROM Wallet w WHERE w.user.id = :userId")
    Optional<BigDecimal> findBlockchainBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(w.balance) FROM Wallet w")
    BigDecimal getTotalSystemBalance();
    
    @Query("SELECT SUM(w.frozenBalance) FROM Wallet w")
    BigDecimal getTotalFrozenBalance();
}