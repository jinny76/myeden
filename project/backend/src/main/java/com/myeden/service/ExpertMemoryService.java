package com.myeden.service;

import com.myeden.entity.ExpertMemory;
import java.util.List;
import java.util.Map;

/**
 * 专家记忆服务接口
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ExpertMemoryService {
    
    /**
     * 加载用户在特定专家主题下的所有记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 记忆列表，按优先级和更新时间排序
     */
    List<ExpertMemory> loadThemeMemories(String userId, String robotId, String themeId);
    
    /**
     * 更新或创建字段记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @param fieldName 字段名称
     * @param content 记忆内容
     * @param memoryType 记忆类型
     * @param priority 优先级(1-5)
     */
    void updateFieldMemory(String userId, String robotId, String themeId, 
                          String fieldName, String content, String memoryType, Integer priority);
    
    /**
     * 批量合并会话记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @param extractedInfo 提取的信息Map（字段名 -> 内容）
     */
    void mergeSessionMemories(String userId, String robotId, String themeId, 
                             Map<String, String> extractedInfo);
    
    /**
     * 获取基础信息记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 基础信息记忆Map（字段名 -> 内容）
     */
    Map<String, String> getBasicInfoMemories(String userId, String robotId, String themeId);
    
    /**
     * 获取会话摘要记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 会话摘要记忆列表
     */
    List<ExpertMemory> getSessionSummaryMemories(String userId, String robotId, String themeId);
    
    /**
     * 获取关键事件记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 关键事件记忆列表
     */
    List<ExpertMemory> getKeyEventMemories(String userId, String robotId, String themeId);
    
    /**
     * 使用AI分析聊天记录并提取记忆
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @param chatHistory 聊天记录文本
     * @param infoFields 需要提取的信息字段列表
     * @return 提取的记忆信息Map
     */
    Map<String, String> extractMemoriesFromChat(String userId, String robotId, String themeId,
                                               String chatHistory, List<String> infoFields);
    
    /**
     * 清理过期记忆
     * 
     * @param daysOld 保留天数
     * @return 清理的记忆数量
     */
    long cleanupOldMemories(int daysOld);
    
    /**
     * 统计用户记忆数量
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 记忆数量
     */
    long countUserMemories(String userId, String robotId, String themeId);
    
    /**
     * 构建记忆上下文字符串，用于AI提示词
     * 
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param themeId 专家主题ID
     * @return 格式化的记忆上下文
     */
    String buildMemoryContext(String userId, String robotId, String themeId);
}