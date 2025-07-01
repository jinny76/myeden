package com.myeden.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * 功能说明：
 * - 提供测试接口验证JWT认证
 * - 调试Spring Security配置
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    /**
     * 公开测试接口
     * @return 测试响应
     */
    @GetMapping("/public")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "公开接口测试成功");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 需要认证的测试接口
     * @return 认证信息
     */
    @GetMapping("/auth")
    public Map<String, Object> authTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "认证接口测试成功");
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal() : null);
        response.put("authorities", authentication != null ? authentication.getAuthorities() : null);
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("认证测试 - 用户: {}, 已认证: {}", 
            authentication != null ? authentication.getName() : "null",
            authentication != null && authentication.isAuthenticated());
        
        return response;
    }
    
    /**
     * 机器人创建测试接口
     * @return 测试响应
     */
    @PostMapping("/robot-create")
    public Map<String, Object> robotCreateTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "机器人创建测试成功");
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        response.put("principal", authentication != null ? authentication.getPrincipal() : null);
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("机器人创建测试 - 用户: {}, 已认证: {}", 
            authentication != null ? authentication.getName() : "null",
            authentication != null && authentication.isAuthenticated());
        
        return response;
    }
} 