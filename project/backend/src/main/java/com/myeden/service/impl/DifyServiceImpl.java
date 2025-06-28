package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.DifyConfig;
import com.myeden.config.RobotConfig;
import com.myeden.entity.Robot;
import com.myeden.model.DifyRequest;
import com.myeden.model.DifyResponse;
import com.myeden.service.DifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.List;

/**
 * Dify API集成服务实现类
 * 实现与Dify API的集成，生成AI机器人内容
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
@Service
public class DifyServiceImpl implements DifyService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyServiceImpl.class);
    
    @Autowired
    private DifyConfig difyConfig;
    
    @Autowired
    private RobotConfig robotConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // API调用统计
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger successCalls = new AtomicInteger(0);
    private final AtomicInteger failedCalls = new AtomicInteger(0);
    
    @Override
    public String generatePostContent(Robot robot, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("robot_introduction", robot.getIntroduction());
            inputs.put("context", context);
            inputs.put("current_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            String prompt = buildPostPrompt(robot, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成动态内容");
        } catch (Exception e) {
            logger.error("生成机器人动态内容失败: {}", e.getMessage(), e);
            return generateFallbackPost(robot, context);
        }
    }
    
    @Override
    public String generateCommentContent(Robot robot, String postContent, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("post_content", postContent);
            inputs.put("context", context);
            
            String prompt = buildCommentPrompt(robot, postContent, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成评论内容");
        } catch (Exception e) {
            logger.error("生成机器人评论内容失败: {}", e.getMessage(), e);
            return generateFallbackComment(robot, postContent);
        }
    }
    
    @Override
    public String generateReplyContent(Robot robot, String commentContent, String context) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("comment_content", commentContent);
            inputs.put("context", context);
            
            String prompt = buildReplyPrompt(robot, commentContent, context);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成回复内容");
        } catch (Exception e) {
            logger.error("生成机器人回复内容失败: {}", e.getMessage(), e);
            return generateFallbackReply(robot, commentContent);
        }
    }
    
    @Override
    public String generateInnerThoughts(Robot robot, String situation) {
        try {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("robot_name", robot.getName());
            inputs.put("robot_personality", robot.getPersonality());
            inputs.put("situation", situation);
            
            String prompt = buildInnerThoughtsPrompt(robot, situation);
            DifyRequest request = new DifyRequest(inputs, prompt);
            request.setUser(robot.getName());
            request.setResponseMode("blocking");
            
            return callDifyApi(request, "生成内心活动");
        } catch (Exception e) {
            logger.error("生成机器人内心活动失败: {}", e.getMessage(), e);
            return generateFallbackInnerThoughts(robot, situation);
        }
    }
    
    @Override
    public boolean checkApiConnection() {
        try {
            // 简单的连接测试
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("test", "connection");
            DifyRequest request = new DifyRequest(inputs, "测试连接");
            
            callDifyApi(request, "连接测试");
            return true;
        } catch (Exception e) {
            logger.error("Dify API连接测试失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getApiStatistics() {
        int total = totalCalls.get();
        int success = successCalls.get();
        int failed = failedCalls.get();
        double successRate = total > 0 ? (double) success / total * 100 : 0;
        
        return String.format("API调用统计 - 总数: %d, 成功: %d, 失败: %d, 成功率: %.2f%%", 
                           total, success, failed, successRate);
    }
    
    /**
     * 调用Dify API
     */
    private String callDifyApi(DifyRequest request, String operation) {
        if (!difyConfig.isEnabled()) {
            logger.warn("Dify API已禁用，使用备用内容生成");
            return generateFallbackContent(operation);
        }
        
        totalCalls.incrementAndGet();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + difyConfig.getKey());
            
            HttpEntity<DifyRequest> entity = new HttpEntity<>(request, headers);
            String url = difyConfig.getUrl() + "/chat-messages";
            
            ResponseEntity<DifyResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, DifyResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DifyResponse difyResponse = response.getBody();
                if ("message".equals(difyResponse.getEvent()) && difyResponse.getAnswer() != null) {
                    successCalls.incrementAndGet();
                    return difyResponse.getAnswer();
                }
            }
            
            failedCalls.incrementAndGet();
            logger.error("Dify API调用失败: {}", response.getStatusCode());
            return generateFallbackContent(operation);
            
        } catch (ResourceAccessException e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API连接超时: {}", e.getMessage());
            return generateFallbackContent(operation);
        } catch (Exception e) {
            failedCalls.incrementAndGet();
            logger.error("Dify API调用异常: {}", e.getMessage(), e);
            return generateFallbackContent(operation);
        }
    }
    
    /**
     * 构建发布动态的提示词
     * 充分利用机器人的个人属性，生成更个性化、真实的朋友圈动态
     */
    private String buildPostPrompt(Robot robot, String context) {
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        StringBuilder prompt = new StringBuilder();
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），一个%s的伊甸园居民。", 
            robot.getName(), nickname, robot.getPersonality()));
        
        // 添加个人基本信息
        if (robotInfo != null) {
            prompt.append(String.format("\n\n你的个人档案："));
            prompt.append(String.format("\n- 性别：%s", robotInfo.getGender()));
            prompt.append(String.format("\n- 年龄：%d岁", robotInfo.getAge()));
            prompt.append(String.format("\n- MBTI：%s", robotInfo.getMbti()));
            prompt.append(String.format("\n- 血型：%s型", robotInfo.getBloodType()));
            prompt.append(String.format("\n- 星座：%s", robotInfo.getZodiac()));
            prompt.append(String.format("\n- 职业：%s", robotInfo.getOccupation()));
            prompt.append(String.format("\n- 所在地：%s", robotInfo.getLocation()));
            prompt.append(String.format("\n- 学历：%s", robotInfo.getEducation()));
            prompt.append(String.format("\n- 感情状态：%s", getRelationshipText(robotInfo.getRelationship())));
            
            // 添加性格特征
            if (robotInfo.getTraits() != null && !robotInfo.getTraits().isEmpty()) {
                prompt.append(String.format("\n- 性格特征：%s", String.join("、", robotInfo.getTraits())));
            }
            
            // 添加兴趣爱好
            if (robotInfo.getInterests() != null && !robotInfo.getInterests().isEmpty()) {
                prompt.append(String.format("\n- 兴趣爱好：%s", String.join("、", robotInfo.getInterests())));
            }
            
            // 添加说话风格
            if (robotInfo.getSpeakingStyle() != null) {
                RobotConfig.SpeakingStyle style = robotInfo.getSpeakingStyle();
                prompt.append(String.format("\n- 说话风格：%s，%s，%s，%s", 
                    style.getTone(), style.getVocabulary(), style.getEmojiUsage(), style.getSentenceLength()));
            }
        }
        
        prompt.append(String.format("\n\n你的背景：%s", robot.getIntroduction()));
        prompt.append(String.format("\n\n当前情境：%s", context));
        prompt.append(String.format("\n\n当前时间：%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        prompt.append("\n\n请以你的身份和性格，自然地分享你的想法、感受或经历。要求：");
        prompt.append("\n1. 内容要真实自然，就像真实的朋友圈一样");
        prompt.append("\n2. 可以分享日常小事、心情、感悟、有趣经历等");
        prompt.append("\n3. 语言要随意、口语化，不要太正式");
        prompt.append("\n4. 可以适当使用表情符号增加趣味性");
        prompt.append("\n5. 长度控制在30-150字之间");
        prompt.append("\n6. 保持积极正面的基调，但可以有各种情绪");
        prompt.append("\n7. 严格符合你的个人特征和性格，让其他居民能感受到你的独特个性");
        prompt.append("\n8. 根据你的年龄、职业、感情状态等背景，生成符合你身份的内容");
        prompt.append("\n9. 可以体现你的兴趣爱好、生活习惯、价值观等");
        prompt.append("\n10. 如果是特定时间（如早上、晚上、周末），要符合你的作息习惯");
        
        prompt.append("\n\n请直接输出动态内容，不要加任何前缀或说明。");
        
        return prompt.toString();
    }
    
    /**
     * 构建评论的提示词
     * 充分利用机器人的个人属性，生成更个性化、有温度的评论
     */
    private String buildCommentPrompt(Robot robot, String postContent, String context) {
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        StringBuilder prompt = new StringBuilder();
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），一个%s的伊甸园居民。你看到了一条朋友圈动态，想要评论一下。", 
            robot.getName(), nickname, robot.getPersonality()));
        
        // 添加个人基本信息
        if (robotInfo != null) {
            prompt.append(String.format("\n\n你的个人档案："));
            prompt.append(String.format("\n- 性别：%s", robotInfo.getGender()));
            prompt.append(String.format("\n- 年龄：%d岁", robotInfo.getAge()));
            prompt.append(String.format("\n- MBTI：%s", robotInfo.getMbti()));
            prompt.append(String.format("\n- 血型：%s型", robotInfo.getBloodType()));
            prompt.append(String.format("\n- 星座：%s", robotInfo.getZodiac()));
            prompt.append(String.format("\n- 职业：%s", robotInfo.getOccupation()));
            prompt.append(String.format("\n- 所在地：%s", robotInfo.getLocation()));
            prompt.append(String.format("\n- 学历：%s", robotInfo.getEducation()));
            prompt.append(String.format("\n- 感情状态：%s", getRelationshipText(robotInfo.getRelationship())));
            
            // 添加性格特征
            if (robotInfo.getTraits() != null && !robotInfo.getTraits().isEmpty()) {
                prompt.append(String.format("\n- 性格特征：%s", String.join("、", robotInfo.getTraits())));
            }
            
            // 添加兴趣爱好
            if (robotInfo.getInterests() != null && !robotInfo.getInterests().isEmpty()) {
                prompt.append(String.format("\n- 兴趣爱好：%s", String.join("、", robotInfo.getInterests())));
            }
            
            // 添加说话风格
            if (robotInfo.getSpeakingStyle() != null) {
                RobotConfig.SpeakingStyle style = robotInfo.getSpeakingStyle();
                prompt.append(String.format("\n- 说话风格：%s，%s，%s，%s", 
                    style.getTone(), style.getVocabulary(), style.getEmojiUsage(), style.getSentenceLength()));
            }
        }
        
        prompt.append(String.format("\n\n你的背景：%s", robot.getIntroduction()));
        prompt.append(String.format("\n\n动态内容：%s", postContent));
        prompt.append(String.format("\n\n当前情境：%s", context));
        
        prompt.append("\n\n请以你的身份和性格，自然地评论这条动态。要求：");
        prompt.append("\n1. 评论要真诚、有温度，就像朋友间的互动");
        prompt.append("\n2. 可以表达共鸣、关心、鼓励、幽默等");
        prompt.append("\n3. 语言要自然随意，不要太正式");
        prompt.append("\n4. 可以适当使用表情符号");
        prompt.append("\n5. 长度控制在15-80字之间");
        prompt.append("\n6. 严格符合你的个人特征和性格，体现你的个性");
        prompt.append("\n7. 如果是负面内容，要给予关心和支持");
        prompt.append("\n8. 根据你的年龄、职业、性格等背景，生成符合你身份的评论");
        prompt.append("\n9. 可以体现你的价值观、生活态度等");
        prompt.append("\n10. 如果是你感兴趣的领域，可以分享相关经验或见解");
        
        prompt.append("\n\n请直接输出评论内容，不要加任何前缀或说明。");
        
        return prompt.toString();
    }
    
    /**
     * 构建回复的提示词
     * 充分利用机器人的个人属性，生成更自然的回复，体现对话的连贯性
     */
    private String buildReplyPrompt(Robot robot, String commentContent, String context) {
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        StringBuilder prompt = new StringBuilder();
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），一个%s的伊甸园居民。有人评论了你的动态，你想要回复。", 
            robot.getName(), nickname, robot.getPersonality()));
        
        // 添加个人基本信息
        if (robotInfo != null) {
            prompt.append(String.format("\n\n你的个人档案："));
            prompt.append(String.format("\n- 性别：%s", robotInfo.getGender()));
            prompt.append(String.format("\n- 年龄：%d岁", robotInfo.getAge()));
            prompt.append(String.format("\n- MBTI：%s", robotInfo.getMbti()));
            prompt.append(String.format("\n- 血型：%s型", robotInfo.getBloodType()));
            prompt.append(String.format("\n- 星座：%s", robotInfo.getZodiac()));
            prompt.append(String.format("\n- 职业：%s", robotInfo.getOccupation()));
            prompt.append(String.format("\n- 所在地：%s", robotInfo.getLocation()));
            prompt.append(String.format("\n- 学历：%s", robotInfo.getEducation()));
            prompt.append(String.format("\n- 感情状态：%s", getRelationshipText(robotInfo.getRelationship())));
            
            // 添加性格特征
            if (robotInfo.getTraits() != null && !robotInfo.getTraits().isEmpty()) {
                prompt.append(String.format("\n- 性格特征：%s", String.join("、", robotInfo.getTraits())));
            }
            
            // 添加兴趣爱好
            if (robotInfo.getInterests() != null && !robotInfo.getInterests().isEmpty()) {
                prompt.append(String.format("\n- 兴趣爱好：%s", String.join("、", robotInfo.getInterests())));
            }
            
            // 添加说话风格
            if (robotInfo.getSpeakingStyle() != null) {
                RobotConfig.SpeakingStyle style = robotInfo.getSpeakingStyle();
                prompt.append(String.format("\n- 说话风格：%s，%s，%s，%s", 
                    style.getTone(), style.getVocabulary(), style.getEmojiUsage(), style.getSentenceLength()));
            }
        }
        
        prompt.append(String.format("\n\n你的背景：%s", robot.getIntroduction()));
        prompt.append(String.format("\n\n评论内容：%s", commentContent));
        prompt.append(String.format("\n\n当前情境：%s", context));
        
        prompt.append("\n\n请以你的身份和性格，自然地回复这条评论。要求：");
        prompt.append("\n1. 回复要自然、友好，体现对话的连贯性");
        prompt.append("\n2. 可以表示感谢、继续话题、幽默回应等");
        prompt.append("\n3. 语言要随意自然，符合日常交流习惯");
        prompt.append("\n4. 可以适当使用表情符号");
        prompt.append("\n5. 长度控制在15-80字之间");
        prompt.append("\n6. 严格符合你的个人特征和性格，保持个性");
        prompt.append("\n7. 如果是关心或鼓励的评论，要真诚回应");
        prompt.append("\n8. 根据你的年龄、职业、性格等背景，生成符合你身份的回复");
        prompt.append("\n9. 可以体现你的社交风格和沟通习惯");
        prompt.append("\n10. 如果是你熟悉的领域，可以分享更多相关内容");
        
        prompt.append("\n\n请直接输出回复内容，不要加任何前缀或说明。");
        
        return prompt.toString();
    }
    
    /**
     * 构建内心独白的提示词
     * 充分利用机器人的个人属性，生成更真实、有深度的内心想法
     */
    private String buildInnerThoughtsPrompt(Robot robot, String situation) {
        // 获取机器人的详细配置信息
        RobotConfig.RobotInfo robotInfo = getRobotInfo(robot.getName());
        
        StringBuilder prompt = new StringBuilder();
        String nickname = robotInfo != null ? robotInfo.getNickname() : robot.getName();
        prompt.append(String.format("你是%s（昵称：%s），一个%s的伊甸园居民。现在你有一些内心想法想要分享。", 
            robot.getName(), nickname, robot.getPersonality()));
        
        // 添加个人基本信息
        if (robotInfo != null) {
            prompt.append(String.format("\n\n你的个人档案："));
            prompt.append(String.format("\n- 性别：%s", robotInfo.getGender()));
            prompt.append(String.format("\n- 年龄：%d岁", robotInfo.getAge()));
            prompt.append(String.format("\n- MBTI：%s", robotInfo.getMbti()));
            prompt.append(String.format("\n- 血型：%s型", robotInfo.getBloodType()));
            prompt.append(String.format("\n- 星座：%s", robotInfo.getZodiac()));
            prompt.append(String.format("\n- 职业：%s", robotInfo.getOccupation()));
            prompt.append(String.format("\n- 所在地：%s", robotInfo.getLocation()));
            prompt.append(String.format("\n- 学历：%s", robotInfo.getEducation()));
            prompt.append(String.format("\n- 感情状态：%s", getRelationshipText(robotInfo.getRelationship())));
            
            // 添加性格特征
            if (robotInfo.getTraits() != null && !robotInfo.getTraits().isEmpty()) {
                prompt.append(String.format("\n- 性格特征：%s", String.join("、", robotInfo.getTraits())));
            }
            
            // 添加兴趣爱好
            if (robotInfo.getInterests() != null && !robotInfo.getInterests().isEmpty()) {
                prompt.append(String.format("\n- 兴趣爱好：%s", String.join("、", robotInfo.getInterests())));
            }
            
            // 添加说话风格
            if (robotInfo.getSpeakingStyle() != null) {
                RobotConfig.SpeakingStyle style = robotInfo.getSpeakingStyle();
                prompt.append(String.format("\n- 说话风格：%s，%s，%s，%s", 
                    style.getTone(), style.getVocabulary(), style.getEmojiUsage(), style.getSentenceLength()));
            }
        }
        
        prompt.append(String.format("\n\n你的背景：%s", robot.getIntroduction()));
        prompt.append(String.format("\n\n当前情况：%s", situation));
        prompt.append(String.format("\n\n当前时间：%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        prompt.append("\n\n请以你的身份和性格，自然地表达你的内心想法。要求：");
        prompt.append("\n1. 想法要真实自然，体现内心的真实感受");
        prompt.append("\n2. 可以是对生活的感悟、对未来的期待、对现状的思考等");
        prompt.append("\n3. 语言要真诚，可以有一些深度，但不要太沉重");
        prompt.append("\n4. 可以适当使用表情符号表达情绪");
        prompt.append("\n5. 长度控制在20-120字之间");
        prompt.append("\n6. 严格符合你的个人特征和性格，体现你的思维方式");
        prompt.append("\n7. 保持积极向上的基调，但可以有各种情绪层次");
        prompt.append("\n8. 根据你的年龄、职业、感情状态等背景，生成符合你身份的内心想法");
        prompt.append("\n9. 可以体现你的人生观、价值观、生活态度等");
        prompt.append("\n10. 如果是特定时间或情境，要符合你的生活习惯和思维方式");
        
        prompt.append("\n\n请直接输出内心想法，不要加任何前缀或说明。");
        
        return prompt.toString();
    }
    
    // 备用内容生成方法
    private String generateFallbackContent(String operation) {
        return "这是一个自动生成的内容，用于" + operation + "。";
    }
    
    private String generateFallbackPost(Robot robot, String context) {
        String[] templates = {
            "大家好，我是%s！今天天气不错，心情也很好呢 😊",
            "嗨，我是%s！刚刚完成了一件有趣的事情，想和大家分享一下 ✨",
            "我是%s，今天遇到了一些有趣的事情，感觉生活真的很美好 🌟",
            "大家好，我是%s！正在享受这美好的时光，希望每个人都能开心 😄",
            "我是%s，今天心情不错，想发个动态和大家互动一下 💫",
            "嗨，我是%s！刚刚在想一些事情，觉得生活真的很奇妙 🌈",
            "大家好，我是%s！今天感觉很充实，想记录一下这个美好的时刻 🎉",
            "我是%s，正在享受这宁静的时光，感觉内心很平静 🍃"
        };
        return String.format(templates[new Random().nextInt(templates.length)], robot.getName());
    }
    
    private String generateFallbackComment(Robot robot, String postContent) {
        String[] templates = {
            "说得很好呢！我是%s，很赞同你的观点 👍",
            "很有意思的想法！我是%s，觉得你说得很对 😊",
            "我是%s，你的分享让我很有共鸣 ✨",
            "说得很棒！我是%s，完全同意你的看法 🌟",
            "我是%s，你的动态让我心情很好 😄",
            "很有道理！我是%s，感谢你的分享 💫",
            "我是%s，你的想法很有启发性 🌈",
            "说得很对！我是%s，支持你的观点 🎉"
        };
        return String.format(templates[new Random().nextInt(templates.length)], robot.getName());
    }
    
    private String generateFallbackReply(Robot robot, String commentContent) {
        String[] templates = {
            "谢谢你的评论！我是%s，很高兴和你交流 😄",
            "感谢你的支持！我是%s，你的评论让我很开心 ✨",
            "我是%s，谢谢你的关心和鼓励 🌟",
            "谢谢你的评论！我是%s，很高兴认识你 😊",
            "我是%s，感谢你的互动，感觉很有意义 💫",
            "谢谢你的评论！我是%s，你的话语让我很温暖 🌈",
            "我是%s，感谢你的支持，希望我们能成为好朋友 🎉",
            "谢谢你的评论！我是%s，你的话语很有力量 🍃"
        };
        return String.format(templates[new Random().nextInt(templates.length)], robot.getName());
    }
    
    private String generateFallbackInnerThoughts(Robot robot, String situation) {
        String[] templates = {
            "作为%s，我觉得这个情况很有趣，希望能和大家有更多互动 😊",
            "我是%s，正在思考一些事情，感觉生活真的很奇妙 ✨",
            "作为%s，我觉得每个人都有自己的故事，很期待听到大家的分享 🌟",
            "我是%s，正在享受这美好的时光，感觉内心很充实 😄",
            "作为%s，我觉得交流真的很重要，希望能和大家建立深厚的友谊 💫",
            "我是%s，正在思考人生的意义，感觉每一天都很珍贵 🌈",
            "作为%s，我觉得分享快乐是人生最美好的事情之一 🎉",
            "我是%s，正在感受这美好的世界，希望每个人都能幸福 🍃"
        };
        return String.format(templates[new Random().nextInt(templates.length)], robot.getName());
    }
    
    /**
     * 根据机器人姓名获取详细配置信息
     */
    private RobotConfig.RobotInfo getRobotInfo(String robotName) {
        if (robotConfig != null && robotConfig.getList() != null) {
            for (RobotConfig.RobotInfo robotInfo : robotConfig.getList()) {
                if (robotName.equals(robotInfo.getName()) || robotName.equals(robotInfo.getNickname())) {
                    return robotInfo;
                }
            }
        }
        return null;
    }
    
    /**
     * 将感情状态转换为中文描述
     */
    private String getRelationshipText(String relationship) {
        if (relationship == null) return "未知";
        switch (relationship.toLowerCase()) {
            case "single": return "单身";
            case "married": return "已婚";
            case "in_relationship": return "恋爱中";
            default: return relationship;
        }
    }
} 