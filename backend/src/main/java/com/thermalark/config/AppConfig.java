package com.thermalark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private TradingConfig trading;
    private IotConfig iot;
    private WalletConfig wallet;
    
    @Data
    public static class TradingConfig {
        private Double feeRate;
        private Double minEnergyAmount;
        private Double maxEnergyAmount;
        private Double priceVolatilityLimit;
    }
    
    @Data
    public static class IotConfig {
        private Integer dataCollectionInterval;
        private Integer dataRetentionDays;
    }
    
    @Data
    public static class WalletConfig {
        private Double initialBalance;
        private Double minDepositAmount;
        private Double minWithdrawAmount;
    }
}