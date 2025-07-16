package com.myeden.service.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 活动数据DTO
 * 
 * 功能说明：
 * - 封装用户在特定日期的活动数据
 * - 包含各类活动的统计信息
 * - 用于前端贡献图的数据展示
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-01-27
 */
public class ActivityData {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 总活动次数
     */
    private Integer totalCount;
    
    /**
     * 各类活动的详细数据
     */
    private List<ActivityDetail> activities;
    
    /**
     * 构造函数
     */
    public ActivityData() {}
    
    public ActivityData(LocalDate date, Integer totalCount, List<ActivityDetail> activities) {
        this.date = date;
        this.totalCount = totalCount;
        this.activities = activities;
    }
    
    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public List<ActivityDetail> getActivities() {
        return activities;
    }
    
    public void setActivities(List<ActivityDetail> activities) {
        this.activities = activities;
    }
    
    /**
     * 活动详情内部类
     */
    public static class ActivityDetail {
        
        /**
         * 活动类型（chat, post, comment, reply, like）
         */
        private String type;
        
        /**
         * 活动次数
         */
        private Integer count;
        
        /**
         * 活动描述
         */
        private String description;
        
        /**
         * 构造函数
         */
        public ActivityDetail() {}
        
        public ActivityDetail(String type, Integer count, String description) {
            this.type = type;
            this.count = count;
            this.description = description;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Integer getCount() {
            return count;
        }
        
        public void setCount(Integer count) {
            this.count = count;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}