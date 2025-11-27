package com.thermalark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI thermalArkOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("热力方舟 API")
                .description("## 基于区块链的P2P热能交易平台\n\n" +
                    "### 主要功能\n" +
                    "- **用户管理**: 用户注册、登录、认证\n" +
                    "- **能源交易**: 热能买卖、订单撮合、交易结算\n" +
                    "- **支付系统**: 微信支付、支付宝支付、银行转账\n" +
                    "- **区块链集成**: 智能合约、代币管理、交易记录\n" +
                    "- **数据监控**: IoT设备数据采集、能源生产监控\n\n" +
                    "### 技术栈\n" +
                    "- **后端**: Spring Boot 3.x, MySQL, Redis\n" +
                    "- **前端**: React, TypeScript, Ant Design\n" +
                    "- **区块链**: Ethereum, Solidity, Web3j\n" +
                    "- **支付**: 微信支付、支付宝支付API\n" +
                    "- **IoT**: Python数据模拟器\n\n" +
                    "### 版本信息\n" +
                    "- **API版本**: v1.0\n" +
                    "- **发布日期**: 2025年1月\n" +
                    "- **开发者**: 热力方舟开发团队")
                .version("v1.0")
                .contact(new Contact()
                    .name("热力方舟开发团队")
                    .email("support@thermalark.com")
                    .url("https://thermalark.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                .description("热力方舟完整文档")
                .url("https://docs.thermalark.com"))
            .servers(Arrays.asList(
                new Server()
                    .url("http://localhost:8080")
                    .description("本地开发环境"),
                new Server()
                    .url("https://api.thermalark.com")
                    .description("生产环境")
            ))
            .tags(Arrays.asList(
                new Tag().name("用户管理").description("用户注册、登录、认证相关接口"),
                new Tag().name("能源交易").description("热能买卖、订单管理、交易撮合接口"),
                new Tag().name("支付系统").description("充值、提现、支付记录接口"),
                new Tag().name("区块链").description("智能合约、代币管理、交易记录接口"),
                new Tag().name("数据监控").description("IoT设备数据、能源生产监控接口"),
                new Tag().name("系统管理").description("系统配置、日志管理、监控接口")
            ))
            .components(new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT认证令牌"))
                .addSecuritySchemes("API Key", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-Key")
                    .description("API密钥认证")))
            .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
}