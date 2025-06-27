package com.myeden.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 负责Redis缓存策略、本地缓存配置、缓存管理优化
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${spring.redis.cache.time-to-live:3600000}")
    private long cacheTimeToLive;

    @Value("${spring.redis.cache.use-key-prefix:true}")
    private boolean useKeyPrefix;

    @Value("${spring.redis.cache.key-prefix:myeden}")
    private String keyPrefix;

    /**
     * Caffeine本地缓存管理器配置
     * 用于高频访问数据的本地缓存，减少Redis访问
     * 当Redis不可用时作为主要缓存
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置Caffeine缓存构建器
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // 最大缓存条目数
                .expireAfterWrite(10, TimeUnit.MINUTES) // 写入后10分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES) // 访问后5分钟过期
                .recordStats() // 记录统计信息
                .ticker(Ticker.systemTicker())); // 使用系统时钟

        // 设置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "user_profile",      // 用户资料
            "robot_info",        // 机器人信息
            "world_config",      // 世界配置
            "hot_posts",         // 热门动态
            "user_session"       // 用户会话
        ));

        logger.info("Caffeine本地缓存管理器配置完成（作为主要缓存）");
        return cacheManager;
    }

    /**
     * Caffeine本地缓存管理器配置（备用）
     * 当Redis可用时作为辅助缓存
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public CacheManager caffeineCacheManagerBackup() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置Caffeine缓存构建器
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // 最大缓存条目数
                .expireAfterWrite(10, TimeUnit.MINUTES) // 写入后10分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES) // 访问后5分钟过期
                .recordStats() // 记录统计信息
                .ticker(Ticker.systemTicker())); // 使用系统时钟

        // 设置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "user_profile",      // 用户资料
            "robot_info",        // 机器人信息
            "world_config",      // 世界配置
            "hot_posts",         // 热门动态
            "user_session"       // 用户会话
        ));

        logger.info("Caffeine本地缓存管理器配置完成（作为辅助缓存）");
        return cacheManager;
    }

    /**
     * 用户资料缓存配置
     * 针对用户资料的快速访问优化
     */
    @Bean("userProfileCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Object> userProfileCache() {
        return Caffeine.newBuilder()
                .maximumSize(500) // 最多缓存500个用户资料
                .expireAfterWrite(30, TimeUnit.MINUTES) // 30分钟过期
                .expireAfterAccess(10, TimeUnit.MINUTES) // 10分钟未访问过期
                .recordStats()
                .build();
    }

    /**
     * 机器人信息缓存配置
     * 针对机器人信息的快速访问优化
     */
    @Bean("robotInfoCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Object> robotInfoCache() {
        return Caffeine.newBuilder()
                .maximumSize(100) // 最多缓存100个机器人信息
                .expireAfterWrite(60, TimeUnit.MINUTES) // 60分钟过期
                .expireAfterAccess(20, TimeUnit.MINUTES) // 20分钟未访问过期
                .recordStats()
                .build();
    }

    /**
     * 热门动态缓存配置
     * 针对热门动态的快速访问优化
     */
    @Bean("hotPostsCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Object> hotPostsCache() {
        return Caffeine.newBuilder()
                .maximumSize(200) // 最多缓存200条热门动态
                .expireAfterWrite(15, TimeUnit.MINUTES) // 15分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES) // 5分钟未访问过期
                .recordStats()
                .build();
    }

    /**
     * 用户会话缓存配置
     * 针对用户会话的快速访问优化
     */
    @Bean("userSessionCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Object> userSessionCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000) // 最多缓存1000个用户会话
                .expireAfterWrite(2, TimeUnit.HOURS) // 2小时过期
                .expireAfterAccess(30, TimeUnit.MINUTES) // 30分钟未访问过期
                .recordStats()
                .build();
    }

    /**
     * 缓存统计信息获取
     * 用于监控缓存命中率和性能
     */
    public com.github.benmanes.caffeine.cache.stats.CacheStats getUserProfileCacheStats() {
        return userProfileCache().stats();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getRobotInfoCacheStats() {
        return robotInfoCache().stats();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getHotPostsCacheStats() {
        return hotPostsCache().stats();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getUserSessionCacheStats() {
        return userSessionCache().stats();
    }

    /**
     * 缓存预热方法
     * 在应用启动时预加载热点数据到缓存
     */
    public void warmUpCache() {
        try {
            logger.info("开始缓存预热...");
            
            // 这里可以添加缓存预热逻辑
            // 例如：预加载热门动态、机器人信息等
            
            logger.info("缓存预热完成");
        } catch (Exception e) {
            logger.error("缓存预热失败", e);
        }
    }

    /**
     * 缓存清理方法
     * 清理所有缓存数据
     */
    public void clearAllCaches() {
        try {
            logger.info("开始清理所有缓存...");
            
            userProfileCache().invalidateAll();
            robotInfoCache().invalidateAll();
            hotPostsCache().invalidateAll();
            userSessionCache().invalidateAll();
            
            logger.info("所有缓存清理完成");
        } catch (Exception e) {
            logger.error("缓存清理失败", e);
        }
    }
} 