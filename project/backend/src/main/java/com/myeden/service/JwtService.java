package com.myeden.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT服务类
 * 
 * 功能说明：
 * - 提供JWT token的生成和验证功能
 * - 支持token的解析和过期时间检查
 * - 提供安全的密钥管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret:myeden-secret-key}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private long expiration;
    
    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成JWT token
     * @param userId 用户ID
     * @return JWT token
     */
    public String generateToken(String userId) {
        return generateToken(new HashMap<>(), userId);
    }
    
    /**
     * 生成JWT token（带额外声明）
     * @param extraClaims 额外声明
     * @param userId 用户ID
     * @return JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String userId) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证JWT token
     * @param token JWT token
     * @param userId 用户ID
     * @return 是否有效
     */
    public boolean isTokenValid(String token, String userId) {
        final String username = extractUserId(token);
        return (username.equals(userId)) && !isTokenExpired(token);
    }
    
    /**
     * 从token中提取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 检查token是否过期
     * @param token JWT token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 提取过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 提取指定声明
     * @param token JWT token
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 提取所有声明
     * @param token JWT token
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }
} 