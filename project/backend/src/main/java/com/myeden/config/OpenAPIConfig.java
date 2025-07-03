package com.myeden.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置类
 * 
 * 功能说明：
 * - 配置 Swagger UI 的 API 文档信息
 * - 添加 JWT 认证支持
 * - 配置服务器信息和联系方式
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class OpenAPIConfig {

    /**
     * 配置 OpenAPI 文档
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 信息
                .info(new Info()
                        .title("我的伊甸园 API")
                        .description("我的伊甸园后端服务 API 文档，提供用户管理、动态发布、AI机器人等功能的 REST API 接口。")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MyEden Team")
                                .email("support@myeden.com")
                                .url("https://myeden.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                
                // 服务器信息
                .servers(List.of(
                        new Server()
                                .url("http://localhost:38080")
                                .description("开发环境服务器"),
                        new Server()
                                .url("https://api.myeden.com")
                                .description("生产环境服务器")
                ))
                
                // 安全配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * 创建 JWT Bearer Token 安全方案
     * @return SecurityScheme 对象
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("JWT Bearer Token 认证。请在登录后获取 token，然后在请求头中添加：Authorization: Bearer {token}");
    }
} 