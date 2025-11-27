package com.thermalark.repository;

import com.thermalark.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByBlockchainAddress(String blockchainAddress);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    boolean existsByBlockchainAddress(String blockchainAddress);
    
    @Query("SELECT u FROM User u WHERE u.isVerified = :verified")
    List<User> findByVerifiedStatus(@Param("verified") Boolean verified);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.UserRole role);
}