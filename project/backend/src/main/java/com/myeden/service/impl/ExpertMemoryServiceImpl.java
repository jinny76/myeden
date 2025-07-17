package com.myeden.service.impl;

import com.myeden.entity.ExpertMemory;
import com.myeden.repository.ExpertMemoryRepository;
import com.myeden.service.ExpertMemoryService;
import com.myeden.service.DifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 专家记忆服务实现类
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ExpertMemoryServiceImpl implements ExpertMemoryService {
    
    @Autowired
    private ExpertMemoryRepository expertMemoryRepository;
    
    @Autowired
    private DifyService difyService;
    
    @Override
    public List<ExpertMemory> loadThemeMemories(String userId, String robotId, String themeId) {
        try {
            log.debug("加载专家主题记忆: userId={}, robotId={}, themeId={}", userId, robotId, themeId);
            
            List<ExpertMemory> memories = expertMemoryRepository
                .findByUserIdAndRobotIdAndThemeIdAndIsActiveTrue(userId, robotId, themeId);
            
            // 按优先级降序、更新时间降序排序
            memories.sort((a, b) -> {
                int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return b.getUpdatedAt().compareTo(a.getUpdatedAt());
            });
            
            log.info("成功加载 {} 条专家主题记忆", memories.size());
            return memories;
            
        } catch (Exception e) {
            log.error("加载专家主题记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void updateFieldMemory(String userId, String robotId, String themeId, 
                                 String fieldName, String content, String memoryType, Integer priority) {
        try {
            log.debug("更新字段记忆: userId={}, robotId={}, themeId={}, fieldName={}", 
                     userId, robotId, themeId, fieldName);
            
            // 查找现有记忆
            Optional<ExpertMemory> existingMemory = expertMemoryRepository
                .findByUserIdAndRobotIdAndThemeIdAndFieldNameAndIsActiveTrue(
                    userId, robotId, themeId, fieldName);
            
            if (existingMemory.isPresent()) {
                // 更新现有记忆
                ExpertMemory memory = existingMemory.get();
                memory.updateContent(content);
                if (priority != null) {
                    memory.setPriority(priority);
                }
                memory.setSummary(generateSummary(content));
                expertMemoryRepository.save(memory);
                log.debug("更新现有记忆: {}", fieldName);
            } else {
                // 创建新记忆
                ExpertMemory newMemory = new ExpertMemory(userId, robotId, themeId, 
                                                        memoryType, fieldName, content);
                if (priority != null) {
                    newMemory.setPriority(priority);
                }
                newMemory.setSummary(generateSummary(content));
                expertMemoryRepository.save(newMemory);
                log.debug("创建新记忆: {}", fieldName);
            }
            
        } catch (Exception e) {
            log.error("更新字段记忆失败: userId={}, robotId={}, themeId={}, fieldName={}", 
                     userId, robotId, themeId, fieldName, e);
        }
    }
    
    @Override
    public void mergeSessionMemories(String userId, String robotId, String themeId, 
                                   Map<String, String> extractedInfo) {
        try {
            log.debug("合并会话记忆: userId={}, robotId={}, themeId={}, fields={}", 
                     userId, robotId, themeId, extractedInfo.keySet());
            
            for (Map.Entry<String, String> entry : extractedInfo.entrySet()) {
                String fieldName = entry.getKey();
                String content = entry.getValue();
                
                if (content != null && !content.trim().isEmpty()) {
                    // 确定记忆类型
                    String memoryType = determineMemoryType(fieldName);
                    // 确定优先级
                    Integer priority = determinePriority(fieldName, memoryType);
                    
                    updateFieldMemory(userId, robotId, themeId, fieldName, content, memoryType, priority);
                }
            }
            
            log.info("成功合并 {} 个字段的会话记忆", extractedInfo.size());
            
        } catch (Exception e) {
            log.error("合并会话记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
        }
    }
    
    @Override
    public Map<String, String> getBasicInfoMemories(String userId, String robotId, String themeId) {
        try {
            List<ExpertMemory> basicMemories = expertMemoryRepository
                .findBasicInfoMemories(userId, robotId, themeId);
            
            return basicMemories.stream()
                .collect(Collectors.toMap(
                    ExpertMemory::getFieldName,
                    ExpertMemory::getContent,
                    (existing, replacement) -> replacement // 如果有重复key，使用新值
                ));
                
        } catch (Exception e) {
            log.error("获取基础信息记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<ExpertMemory> getSessionSummaryMemories(String userId, String robotId, String themeId) {
        try {
            return expertMemoryRepository.findSessionSummaryMemories(userId, robotId, themeId);
        } catch (Exception e) {
            log.error("获取会话摘要记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<ExpertMemory> getKeyEventMemories(String userId, String robotId, String themeId) {
        try {
            return expertMemoryRepository.findKeyEventMemories(userId, robotId, themeId);
        } catch (Exception e) {
            log.error("获取关键事件记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, String> extractMemoriesFromChat(String userId, String robotId, String themeId,
                                                      String chatHistory, List<String> infoFields) {
        try {
            log.debug("使用AI提取记忆: userId={}, robotId={}, themeId={}", userId, robotId, themeId);
            
            // 构建记忆提取提示词
            String extractionPrompt = buildMemoryExtractionPrompt(chatHistory, infoFields);
            
            // 调用Dify API进行记忆提取
            DifyService.DifyChatResult result = difyService.callDifyApi(
                extractionPrompt, robotId, null, null);
            
            if (result != null && result.answer != null) {
                // 解析AI返回的结果
                Map<String, String> extractedMemories = parseExtractionResult(result.answer, infoFields);
                
                log.info("AI提取记忆成功，提取到 {} 个字段", extractedMemories.size());
                return extractedMemories;
            }
            
            log.warn("AI提取记忆返回空结果");
            return new HashMap<>();
            
        } catch (Exception e) {
            log.error("AI提取记忆失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public long cleanupOldMemories(int daysOld) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            List<ExpertMemory> oldMemories = expertMemoryRepository
                .findByCreatedAtBeforeAndIsActiveTrue(cutoffDate);
            
            long count = oldMemories.size();
            for (ExpertMemory memory : oldMemories) {
                memory.deactivate();
                expertMemoryRepository.save(memory);
            }
            
            log.info("清理了 {} 条过期记忆（超过{}天）", count, daysOld);
            return count;
            
        } catch (Exception e) {
            log.error("清理过期记忆失败", e);
            return 0;
        }
    }
    
    @Override
    public long countUserMemories(String userId, String robotId, String themeId) {
        try {
            return expertMemoryRepository.countByUserIdAndRobotIdAndThemeIdAndIsActiveTrue(
                userId, robotId, themeId);
        } catch (Exception e) {
            log.error("统计用户记忆数量失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return 0;
        }
    }
    
    @Override
    public String buildMemoryContext(String userId, String robotId, String themeId) {
        try {
            List<ExpertMemory> memories = loadThemeMemories(userId, robotId, themeId);
            
            if (memories.isEmpty()) {
                return "";
            }
            
            StringBuilder context = new StringBuilder();
            context.append("## 上次沟通已知用户信息：\n");
            
            // 按类型分组记忆
            Map<String, List<ExpertMemory>> groupedMemories = memories.stream()
                .collect(Collectors.groupingBy(ExpertMemory::getMemoryType));
            
            // 基础信息
            List<ExpertMemory> basicInfo = groupedMemories.get("basic_info");
            if (basicInfo != null && !basicInfo.isEmpty()) {
                context.append("### 基础信息：\n");
                for (ExpertMemory memory : basicInfo) {
                    context.append(String.format("- %s：%s\n", 
                                               memory.getFieldName(), memory.getContent()));
                }
                context.append("\n");
            }
            
            // 会话摘要
            List<ExpertMemory> sessionSummaries = groupedMemories.get("session_summary");
            if (sessionSummaries != null && !sessionSummaries.isEmpty()) {
                context.append("### 咨询历史：\n");
                for (ExpertMemory memory : sessionSummaries) {
                    context.append(String.format("- %s：%s\n", 
                                               memory.getFieldName(), memory.getContent()));
                }
                context.append("\n");
            }
            
            // 关键事件
            List<ExpertMemory> keyEvents = groupedMemories.get("key_event");
            if (keyEvents != null && !keyEvents.isEmpty()) {
                context.append("### 重要节点：\n");
                for (ExpertMemory memory : keyEvents) {
                    context.append(String.format("- %s：%s\n", 
                                               memory.getFieldName(), memory.getContent()));
                }
                context.append("\n");
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.error("构建记忆上下文失败: userId={}, robotId={}, themeId={}", 
                     userId, robotId, themeId, e);
            return "";
        }
    }
    
    // 私有辅助方法
    
    /**
     * 生成内容摘要
     */
    private String generateSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // 简单的摘要生成：取前50个字符
        String summary = content.trim();
        if (summary.length() > 50) {
            summary = summary.substring(0, 47) + "...";
        }
        return summary;
    }
    
    /**
     * 确定记忆类型
     */
    private String determineMemoryType(String fieldName) {
        // 基础信息字段
        if (fieldName.contains("基本情况") || fieldName.contains("年龄") || 
            fieldName.contains("职业") || fieldName.contains("家庭")) {
            return "basic_info";
        }
        
        // 关键事件字段
        if (fieldName.contains("突破") || fieldName.contains("决定") || 
            fieldName.contains("危机") || fieldName.contains("重要")) {
            return "key_event";
        }
        
        // 默认为会话摘要
        return "session_summary";
    }
    
    /**
     * 确定优先级
     */
    private Integer determinePriority(String fieldName, String memoryType) {
        // 基础信息优先级最高
        if ("basic_info".equals(memoryType)) {
            return 5;
        }
        
        // 关键事件优先级次之
        if ("key_event".equals(memoryType)) {
            return 4;
        }
        
        // 会话摘要优先级较低
        return 3;
    }
    
    /**
     * 构建记忆提取提示词
     */
    private String buildMemoryExtractionPrompt(String chatHistory, List<String> infoFields) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请分析以下对话记录，提取用户的关键信息。\n\n");
        prompt.append("## 对话记录：\n");
        prompt.append(chatHistory);
        prompt.append("\n\n## 重点需要提取的信息字段：\n");
        
        for (String field : infoFields) {
            prompt.append("- ").append(field).append("\n");
        }
        
        prompt.append("\n## 提取要求：\n");
        prompt.append("1. 只提取对话中明确提到的信息，不要推测或编造\n");
        prompt.append("2. 对信息进行提取，不局限重点提取信息字段，例如教育情况，爱好，朋友，家庭等\n");
        prompt.append("3. 如果某个字段没有相关信息，请返回空值\n");
        prompt.append("4. 提取的信息要准确、简洁\n");
        prompt.append("5. 返回格式为JSON，字段名作为key，提取的内容作为value\n");
        prompt.append("6. 只返回JSON，不要其他解释\n\n");
        
        prompt.append("## 示例格式：\n");
        prompt.append("{\n");
        prompt.append("  \"基本情况\": \"30岁软件工程师，单身\",\n");
        prompt.append("  \"困惑点\": \"工作压力大，经常加班导致焦虑\",\n");
        prompt.append("  \"期望目标\": \"希望学会管理情绪，提高工作效率\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析AI提取结果
     */
    private Map<String, String> parseExtractionResult(String aiResult, List<String> infoFields) {
        Map<String, String> result = new HashMap<>();
        try {
            String jsonContent = aiResult.trim();
            if (!jsonContent.startsWith("{") && jsonContent.endsWith("}")) {
                jsonContent = jsonContent.substring(jsonContent.indexOf("{"));
            }
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonContent);
            if (node.isObject()) {
                node.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue().asText("");
                    if (!value.isEmpty() && !"null".equals(value)) {
                        result.put(key, value);
                    }
                });
            }
        } catch (Exception e) {
            log.error("解析AI提取结果失败: {}", aiResult, e);
        }
        return result;
    }
}