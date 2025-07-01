package com.myeden.config;

import com.myeden.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * 功能说明：
 * - 从请求头中提取JWT token
 * - 验证token的有效性
 * - 设置用户认证信息到SecurityContext
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 获取Authorization头
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;
        
        // 检查Authorization头是否存在且格式正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization头不存在或格式不正确: {}", authHeader);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 提取JWT token
        jwt = authHeader.substring(7);
        
        try {
            // 从token中提取用户ID
            userId = jwtService.extractUserId(jwt);
            
            logger.info("从JWT token中提取的用户ID: {}", userId);
            logger.debug("请求URL: {}, 方法: {}", request.getRequestURI(), request.getMethod());
            
            // 如果成功提取用户ID且当前没有认证信息
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("开始加载用户详情，用户ID: {}", userId);
                
                try {
                    // 加载用户详情
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);
                    logger.debug("用户详情加载成功: {}", userDetails.getUsername());
                    
                    // 验证token
                    if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                        logger.debug("Token验证成功，创建认证token");
                        
                        // 创建认证token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        
                        // 设置认证详情
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置认证信息到SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("认证信息已设置到SecurityContext");
                    } else {
                        logger.warn("Token验证失败，用户ID: {}", userId);
                    }
                } catch (Exception e) {
                    logger.error("加载用户详情失败，用户ID: {}", userId, e);
                }
            } else {
                if (userId == null) {
                    logger.debug("无法从token中提取用户ID");
                } else {
                    logger.debug("SecurityContext中已有认证信息");
                }
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常，让请求继续处理
            logger.error("JWT认证失败: {}", e.getMessage(), e);
            logger.debug("请求URL: {}, 方法: {}, Authorization头: {}", 
                request.getRequestURI(), request.getMethod(), authHeader);
        }
        
        // 记录当前认证状态
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("过滤器结束 - 请求URL: {}, 认证状态: {}, 用户: {}", 
            request.getRequestURI(), 
            currentAuth != null && currentAuth.isAuthenticated() ? "已认证" : "未认证",
            currentAuth != null ? currentAuth.getName() : "null");
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
} 