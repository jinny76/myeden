package com.myeden.service;

/**
 * 用户消息监控服务接口
 * 为每个用户维护一个定时扫描线程，监控Coze会话中的新消息
 */
public interface UserMessageMonitorService {
    
    /**
     * 开始监控指定用户的消息
     * 
     * @param wechatUserId 微信用户ID
     */
    void startMonitoring(String wechatUserId);
    
    /**
     * 停止监控指定用户的消息
     * 
     * @param wechatUserId 微信用户ID
     */
    void stopMonitoring(String wechatUserId);
    
    /**
     * 检查并处理用户的新消息
     * 
     * @param wechatUserId 微信用户ID
     * @return 是否发现新消息
     */
    boolean checkAndProcessNewMessages(String wechatUserId);
    
    /**
     * 停止所有监控任务
     */
    void stopAllMonitoring();
    
    /**
     * 获取当前活跃监控用户数量
     * 
     * @return 监控用户数量
     */
    int getActiveMonitoringCount();
    
    /**
     * 重启指定用户的监控（重新创建监控线程）
     * 
     * @param wechatUserId 微信用户ID
     */
    void restartMonitoring(String wechatUserId);
}