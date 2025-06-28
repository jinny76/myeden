package com.myeden.service;

import com.myeden.entity.Robot;

/**
 * 机器人行为管理服务接口
 * 负责管理AI机器人的行为触发、时机控制和状态管理
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
public interface RobotBehaviorService {
    
    /**
     * 触发机器人发布动态
     * 根据机器人配置和当前时间判断是否应该发布动态
     * 
     * @param robotId 机器人ID
     * @return 是否成功触发
     */
    boolean triggerRobotPost(String robotId);
    
    /**
     * 触发机器人发表评论
     * 根据机器人配置和动态内容判断是否应该发表评论
     * 
     * @param robotId 机器人ID
     * @param postId 动态ID
     * @return 是否成功触发
     */
    boolean triggerRobotComment(String robotId, String postId);
    
    /**
     * 触发机器人回复评论
     * 根据机器人配置和评论内容判断是否应该回复
     * 
     * @param robotId 机器人ID
     * @param commentId 评论ID
     * @return 是否成功触发
     */
    boolean triggerRobotReply(String robotId, String commentId);
    
    /**
     * 检查机器人是否处于活跃状态
     * 根据当前时间和机器人配置判断是否在活跃时间段
     * 
     * @param robot 机器人信息
     * @return 是否活跃
     */
    boolean isRobotActive(Robot robot);
    
    /**
     * 计算行为触发概率
     * 根据机器人配置、当前时间和上下文计算行为触发概率
     * 
     * @param robot 机器人信息
     * @param behaviorType 行为类型（post/comment/reply）
     * @param context 上下文信息
     * @return 触发概率（0-1之间）
     */
    double calculateBehaviorProbability(Robot robot, String behaviorType, String context, Boolean isRobot);
    
    /**
     * 获取机器人今日行为统计
     * 统计机器人今日已发布动态、评论、回复的数量
     * 
     * @param robotId 机器人ID
     * @return 行为统计信息
     */
    String getRobotDailyStats(String robotId);
    
    /**
     * 重置机器人行为统计
     * 重置机器人今日的行为统计计数
     * 
     * @param robotId 机器人ID
     */
    void resetRobotDailyStats(String robotId);
    
    /**
     * 重置所有机器人每日统计
     * 定时任务调用的方法
     */
    void resetDailyStats();
    
    /**
     * 定时触发机器人行为
     * 每分钟执行一次，随机触发机器人发布动态或评论
     */
    void scheduledRobotBehavior();
    
    /**
     * 启动机器人行为调度
     * 启动定时任务，定期触发机器人行为
     */
    void startBehaviorScheduler();
    
    /**
     * 停止机器人行为调度
     * 停止定时任务
     */
    void stopBehaviorScheduler();
    
    /**
     * 触发所有在线机器人对指定动态进行评论
     * 当有新动态发布时，自动触发所有符合条件的机器人进行AI评论
     * 
     * @param postId 动态ID
     * @param postContent 动态内容（用于日志记录）
     * @return 成功触发的机器人数量
     */
    void triggerAllRobotsComment(String postId, String postContent);
    
    /**
     * 刷新机器人在线状态
     * 根据机器人的活跃时间配置更新数据库中的isActive状态
     * 并推送WebSocket消息通知状态变化
     */
    void refreshRobotActiveStatus();
} 