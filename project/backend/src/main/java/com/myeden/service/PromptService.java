package com.myeden.service;

import com.myeden.entity.Robot;
import java.util.List;

/**
 * 提示词服务接口
 * 负责生成AI机器人的提示词、内容处理和上下文构建
 * 与具体的AI服务提供商解耦，专注于提示词工程和内容处理逻辑
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface PromptService {
    
    /**
     * 构建机器人动态生成提示词
     * 根据机器人配置和上下文构建用于生成动态的提示词
     * 
     * @param robot 机器人信息
     * @param context 上下文信息（如当前时间、天气、最近事件等）
     * @return 构建的提示词
     */
    String buildPostPrompt(Robot robot, String context);
    
    /**
     * 构建机器人评论生成提示词
     * 根据机器人配置、动态内容和上下文构建用于生成评论的提示词
     * 
     * @param robot 机器人信息
     * @param post 动态内容
     * @param context 上下文信息
     * @return 构建的提示词
     */
    String buildCommentPrompt(Robot robot, PostService.PostDetail post, String context);
    
    /**
     * 构建机器人回复生成提示词
     * 根据机器人配置、评论内容和上下文构建用于生成回复的提示词
     * 
     * @param robot 机器人信息
     * @param commentDetail 评论内容
     * @param postDetail 动态内容
     * @param context 上下文信息
     * @return 构建的提示词
     */
    String buildReplyPrompt(Robot robot, CommentService.CommentDetail commentDetail, PostService.PostDetail postDetail, String context);
    
    /**
     * 构建机器人内心活动提示词
     * 根据机器人配置和当前情况构建用于生成内心独白的提示词
     * 
     * @param robot 机器人信息
     * @param situation 当前情况描述
     * @return 构建的提示词
     */
    String buildInnerThoughtsPrompt(Robot robot, String situation);
    
    /**
     * 处理AI生成的内容
     * 对AI生成的内容进行后处理，如清理格式、验证长度、添加表情等
     * 
     * @param rawContent AI生成的原始内容
     * @param robot 机器人信息
     * @param contentType 内容类型（post/comment/reply）
     * @return 处理后的内容
     */
    String processGeneratedContent(String rawContent, Robot robot, String contentType);
    
    /**
     * 构建上下文信息
     * 根据当前时间、天气、最近事件等构建上下文信息
     * 
     * @param robot 机器人信息
     * @return 构建的上下文信息
     */
    String buildContext(Robot robot);
    
    /**
     * 验证生成内容的合理性
     * 检查生成的内容是否符合机器人的性格特征和行为模式
     * 
     * @param content 生成的内容
     * @param robot 机器人信息
     * @param contentType 内容类型
     * @return 内容是否合理
     */
    boolean validateContent(String content, Robot robot, String contentType);
    
    /**
     * 获取机器人个性化设定
     * 根据机器人配置生成个性化的设定描述
     * 
     * @param robot 机器人信息
     * @return 个性化设定描述
     */
    String getRobotPersonality(Robot robot);
    
    /**
     * 随机选择一个主题
     * 从通用主题和机器人个人主题中根据频次随机选择一个主题
     * 
     * @param robot 机器人信息
     * @return 选中的主题内容
     */
    String selectRandomTopic(Robot robot);
    
    /**
     * 获取合并后的主题列表
     * 将通用主题和机器人个人主题合并，并根据频次进行权重计算
     * 
     * @param robot 机器人信息
     * @return 合并后的主题列表
     */
    List<TopicItem> getMergedTopics(Robot robot);
    
    /**
     * 主题项类
     */
    class TopicItem {
        private String name;
        private String content;
        private int frequency;
        private String source; // "common" 或 "personal"
        
        public TopicItem(String name, String content, int frequency, String source) {
            this.name = name;
            this.content = content;
            this.frequency = frequency;
            this.source = source;
        }
        
        // Getter方法
        public String getName() { return name; }
        public String getContent() { return content; }
        public int getFrequency() { return frequency; }
        public String getSource() { return source; }
    }

    String generatePostContent(Robot robot, String context);

    String generateCommentContent(Robot robot, PostService.PostDetail post, String context);

    String generateReplyContent(Robot robot, CommentService.CommentDetail commentDetail, PostService.PostDetail postDetail, String context);

    String generateInnerThoughts(Robot robot, String situation);
}