package com.myeden.config;

import com.myeden.controller.EventResponse;
import com.myeden.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 身份验证切面
 * 
 * 功能说明：
 * - 处理@RequireAuthentication注解
 * - 提供细粒度的授权检查
 * - 确保用户只能操作自己的资源
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Aspect
@Component
public class AuthenticationAspect {
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * 环绕通知：处理@RequireAuthentication注解
     * 
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(com.myeden.config.RequireAuthentication)")
    public Object authenticate(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 检查是否已认证
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(EventResponse.error(401, "未提供有效的认证token"));
        }
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAuthentication authAnnotation = method.getAnnotation(RequireAuthentication.class);
        
        // 如果需要所有权检查
        if (authAnnotation.requireOwnership()) {
            // 从认证信息中获取当前用户ID
            String currentUserId = authentication.getName();
            
            // 获取方法参数中的userId
            String targetUserId = extractUserIdFromParameters(joinPoint.getArgs(), signature.getParameterNames());
            
            if (targetUserId != null && !currentUserId.equals(targetUserId)) {
                return ResponseEntity.status(403).body(EventResponse.error(403, "只能操作自己的资源"));
            }
        }
        
        // 继续执行原方法
        return joinPoint.proceed();
    }
    
    /**
     * 从方法参数中提取userId
     * 
     * @param args 方法参数值
     * @param paramNames 参数名
     * @return userId，如果未找到则返回null
     */
    private String extractUserIdFromParameters(Object[] args, String[] paramNames) {
        if (args == null || paramNames == null) {
            return null;
        }
        
        for (int i = 0; i < paramNames.length && i < args.length; i++) {
            if ("userId".equals(paramNames[i]) && args[i] instanceof String) {
                return (String) args[i];
            }
        }
        
        return null;
    }
} 