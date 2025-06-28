package com.myeden.service;

import com.myeden.entity.Robot;

/**
 * Dify API集成服务接口
 * 负责调用Dify API生成AI机器人的动态、评论和回复内容
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
public interface DifyService {
    
    /**
     * 生成机器人动态内容
     * 根据机器人配置和上下文生成符合机器人性格的动态内容
     * 
     * @param robot 机器人信息
     * @param context 上下文信息（如当前时间、天气、最近事件等）
     * @return 生成的动态内容
     */
    String generatePostContent(Robot robot, String context);
    
    /**
     * 生成机器人评论内容
     * 根据机器人配置、动态内容和上下文生成评论
     * 
     * @param robot 机器人信息
     * @param post 动态内容
     * @param context 上下文信息
     * @return 生成的评论内容
     */
    String generateCommentContent(Robot robot, PostService.PostDetail post, String context);
    
    /**
     * 生成机器人回复内容
     * 根据机器人配置、评论内容和上下文生成回复
     * 
     * @param robot 机器人信息
     * @param commentDetail 评论内容
     * @param context 上下文信息
     * @return 生成的回复内容
     */
    String generateReplyContent(Robot robot, CommentService.CommentDetail commentDetail, PostService.PostDetail postDetail, String context);
    
    /**
     * 生成机器人内心活动
     * 根据机器人配置和当前情况生成内心独白
     * 
     * @param robot 机器人信息
     * @param situation 当前情况描述
     * @return 生成的内心活动内容
     */
    String generateInnerThoughts(Robot robot, String situation);
    
    /**
     * 检查Dify API连接状态
     * 验证API密钥和连接是否正常
     * 
     * @return 连接是否正常
     */
    boolean checkApiConnection();
    
    /**
     * 获取API调用统计信息
     * 返回当前API调用次数、成功率等信息
     * 
     * @return API统计信息
     */
    String getApiStatistics();
} 