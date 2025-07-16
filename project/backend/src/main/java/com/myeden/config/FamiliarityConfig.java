package com.myeden.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 熟悉度配置类
 * 
 * 功能说明：
 * - 从application.yml读取熟悉度相关配置
 * - 提供积分计算和等级判断的配置参数
 * - 支持动态调整积分值和等级阈值
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Configuration
@ConfigurationProperties(prefix = "familiarity")
public class FamiliarityConfig {
    
    /**
     * 行为积分配置
     */
    private Map<String, Integer> points = new HashMap<>();
    
    /**
     * 等级阈值配置
     */
    private Map<String, Integer> levels = new HashMap<>();
    
    public Map<String, Integer> getPoints() {
        return points;
    }
    
    public void setPoints(Map<String, Integer> points) {
        this.points = points;
    }
    
    public Map<String, Integer> getLevels() {
        return levels;
    }
    
    public void setLevels(Map<String, Integer> levels) {
        this.levels = levels;
    }
    
    /**
     * 根据行为类型获取积分
     * 
     * @param actionType 行为类型（chat, comment, reply, like）
     * @return 积分值，如果未配置则返回0
     */
    public Integer getPointsByAction(String actionType) {
        return points.getOrDefault(actionType.toLowerCase(), 0);
    }
    
    /**
     * 根据等级名称获取阈值
     * 
     * @param levelName 等级名称
     * @return 阈值，如果未配置则返回0
     */
    public Integer getLevelThreshold(String levelName) {
        return levels.getOrDefault(levelName.toLowerCase().replace("-", "-"), 0);
    }
    
    /**
     * 根据积分获取等级
     * 
     * @param score 积分值
     * @return 等级（0-4）
     */
    public Integer getLevelByScore(int score) {
        if (score >= getLevelThreshold("intimate")) {
            return 4; // 密友
        } else if (score >= getLevelThreshold("close-friend")) {
            return 3; // 好友
        } else if (score >= getLevelThreshold("friend")) {
            return 2; // 朋友
        } else if (score >= getLevelThreshold("acquaintance")) {
            return 1; // 熟人
        } else {
            return 0; // 陌生人
        }
    }
    
    /**
     * 根据等级获取名称
     * 
     * @param level 等级（0-4）
     * @return 等级名称
     */
    public String getLevelName(int level) {
        switch (level) {
            case 0:
                return "陌生人";
            case 1:
                return "熟人";
            case 2:
                return "朋友";
            case 3:
                return "好友";
            case 4:
                return "密友";
            default:
                return "未知";
        }
    }
}