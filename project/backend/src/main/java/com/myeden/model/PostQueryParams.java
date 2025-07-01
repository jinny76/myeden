package com.myeden.model;

import java.util.List;

/**
 * 动态查询参数类
 * 
 * 功能说明：
 * - 封装动态查询的所有参数
 * - 支持分页、过滤、搜索等功能
 * - 提供参数验证和默认值设置
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class PostQueryParams {
    
    /**
     * 页码，从1开始
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 作者类型过滤：user/robot
     */
    private String authorType;
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 当前用户ID，用于权限控制
     */
    private String currentUserId;
    
    /**
     * 已连接机器人ID列表，用于机器人动态过滤
     */
    private List<String> connectedRobotIds;
    
    /**
     * 是否只显示最新动态（按创建时间排序）
     */
    private Boolean isLatest = true;
    
    // 构造函数
    public PostQueryParams() {}
    
    public PostQueryParams(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }
    
    // Getter和Setter方法
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page != null ? page : 1;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size != null ? size : 10;
    }
    
    public String getAuthorType() {
        return authorType;
    }
    
    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getCurrentUserId() {
        return currentUserId;
    }
    
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
    
    public List<String> getConnectedRobotIds() {
        return connectedRobotIds;
    }
    
    public void setConnectedRobotIds(List<String> connectedRobotIds) {
        this.connectedRobotIds = connectedRobotIds;
    }
    
    public Boolean getIsLatest() {
        return isLatest;
    }
    
    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest != null ? isLatest : true;
    }
    
    /**
     * 验证参数有效性
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }
        if (isLatest == null) {
            isLatest = true;
        }
    }
    
    /**
     * 检查是否有搜索条件
     */
    public boolean hasSearchCondition() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 检查是否有作者类型过滤
     */
    public boolean hasAuthorTypeFilter() {
        return authorType != null && !authorType.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "PostQueryParams{" +
                "page=" + page +
                ", size=" + size +
                ", authorType='" + authorType + '\'' +
                ", keyword='" + keyword + '\'' +
                ", currentUserId='" + currentUserId + '\'' +
                ", connectedRobotIds=" + connectedRobotIds +
                ", isLatest=" + isLatest +
                '}';
    }
} 