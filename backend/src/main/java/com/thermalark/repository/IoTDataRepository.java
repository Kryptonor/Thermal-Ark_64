package com.thermalark.repository;

import com.thermalark.entity.IoTData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IoTDataRepository extends JpaRepository<IoTData, Long> {
    List<IoTData> findByDeviceId(String deviceId);
    List<IoTData> findByUserId(Long userId);
    
    @Query("SELECT i FROM IoTData i WHERE i.deviceId = :deviceId AND i.timestamp BETWEEN :startTime AND :endTime ORDER BY i.timestamp ASC")
    List<IoTData> findByDeviceIdAndTimeRange(@Param("deviceId") String deviceId, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT i FROM IoTData i WHERE i.timestamp BETWEEN :startTime AND :endTime AND i.isActive = true")
    List<IoTData> findActiveDataByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT AVG(i.temperature) FROM IoTData i WHERE i.deviceId = :deviceId AND i.timestamp BETWEEN :startTime AND :endTime")
    BigDecimal getAverageTemperature(@Param("deviceId") String deviceId, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT SUM(i.energyOutput) FROM IoTData i WHERE i.deviceId = :deviceId AND i.timestamp BETWEEN :startTime AND :endTime")
    BigDecimal getTotalEnergyOutput(@Param("deviceId") String deviceId, 
                                  @Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);
}