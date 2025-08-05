package com.myeden.service.impl;

import com.myeden.entity.Robot;
import com.myeden.entity.GroupChatMessage;
import com.myeden.entity.ChatRoomMember;
import com.myeden.service.ChatroomPromptService;
import com.myeden.service.ChatRoomMemberService;
import com.myeden.repository.RobotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 聊天室提示词服务实现类
 * 专门管理聊天室相关的提示词模板和内容生成
 *
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-08-05
 */
@Service
public class ChatroomPromptServiceImpl implements ChatroomPromptService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatroomPromptServiceImpl.class);
    
    private Map<String, Object> promptConfig;
    private final Random random = new Random();
    
    @Autowired
    private ChatRoomMemberService memberService;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @PostConstruct
    public void loadPromptConfig() {
        try {
            Yaml yaml = new Yaml();
            ClassPathResource resource = new ClassPathResource("prompts/chatroom-prompts.yaml");
            try (InputStream inputStream = resource.getInputStream()) {
                promptConfig = yaml.load(inputStream);
                logger.info("聊天室提示词配置加载成功");
            }
        } catch (Exception e) {
            logger.error("加载聊天室提示词配置失败", e);
            // 使用默认配置
            promptConfig = getDefaultConfig();
        }
    }
    
    @Override
    public String buildBasicChatPrompt(Robot robot, String chatContext) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> basicChatConfig = getMapFromConfig(chatroomConfig, "basic_chat");
            String template = (String) basicChatConfig.get("template");
            
            // 随机决定发送1-3条消息
            int messageCount = 1 + random.nextInt(3); // 1, 2, 或 3
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nickname", robot.getNickname());
            variables.put("personality", getPersonalityDescription(robot));
            variables.put("chat_context", chatContext != null ? chatContext : "这是一个新的对话开始。");
            variables.put("member_info", ""); // 基础方法不包含成员信息
            variables.put("message_count", String.valueOf(messageCount));
            variables.put("split_instruction", getSplitInstruction(messageCount));
            
            String result = processTemplate(template, variables);
            logger.debug("构建的基础聊天提示词: robotId={}, messageCount={}, 长度={}", 
                       robot.getRobotId(), messageCount, result.length());
            return result;
            
        } catch (Exception e) {
            logger.error("构建基础聊天提示词失败", e);
            return getDefaultBasicChatPrompt(robot, chatContext);
        }
    }
    
    @Override
    public String buildReplyToUserPrompt(Robot robot, GroupChatMessage userMessage, String chatContext) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> replyConfig = getMapFromConfig(chatroomConfig, "reply_to_user");
            
            // 随机决定发送1-2条消息（回复比较简短）
            int messageCount = 1 + random.nextInt(2); // 1 或 2
            
            // 判断是否是直接提及
            String template;
            if (userMessage.getContent().contains(robot.getNickname()) || 
                userMessage.getContent().contains("@" + robot.getNickname())) {
                template = (String) replyConfig.get("direct_mention_template");
            } else {
                template = (String) replyConfig.get("template");
            }
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nickname", robot.getNickname());
            variables.put("personality", getPersonalityDescription(robot));
            variables.put("chat_context", chatContext != null ? chatContext : "");
            variables.put("user_nickname", userMessage.getSenderNickname() != null ? 
                         userMessage.getSenderNickname() : "用户");
            variables.put("user_message", userMessage.getContent());
            variables.put("message_count", String.valueOf(messageCount));
            variables.put("split_instruction", getSplitInstruction(messageCount));
            
            return processTemplate(template, variables);
            
        } catch (Exception e) {
            logger.error("构建回复用户提示词失败", e);
            return getDefaultReplyPrompt(robot, userMessage);
        }
    }
    
    @Override
    public String buildTopicSwitchPrompt(Robot robot, LocalDateTime currentTime) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> topicConfig = getMapFromConfig(chatroomConfig, "topic_switch");
            String template = (String) topicConfig.get("template");
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nickname", robot.getNickname());
            variables.put("personality", getPersonalityDescription(robot));
            
            return processTemplate(template, variables);
            
        } catch (Exception e) {
            logger.error("构建话题切换提示词失败", e);
            return getDefaultTopicSwitchPrompt(robot);
        }
    }
    
    @Override
    public String getFallbackResponse(Robot robot) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> basicChatConfig = getMapFromConfig(chatroomConfig, "basic_chat");
            List<String> fallbackResponses = (List<String>) basicChatConfig.get("fallback_responses");
            
            if (fallbackResponses != null && !fallbackResponses.isEmpty()) {
                return fallbackResponses.get(random.nextInt(fallbackResponses.size()));
            }
            
        } catch (Exception e) {
            logger.error("获取后备回复失败", e);
        }
        
        // 默认后备回复
        String[] defaults = {
            "大家聊得很开心呢！", 
            "我也来参与一下讨论~", 
            "这个话题很有意思！"
        };
        return defaults[random.nextInt(defaults.length)];
    }
    
    @Override
    public String getTimeBasedGreeting(Robot robot, LocalDateTime currentTime) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> greetingConfig = getMapFromConfig(chatroomConfig, "time_based_greetings");
            
            String timeSlot = getTimeSlot(currentTime.toLocalTime());
            List<String> greetings = (List<String>) greetingConfig.get(timeSlot);
            
            if (greetings != null && !greetings.isEmpty()) {
                return greetings.get(random.nextInt(greetings.size()));
            }
            
        } catch (Exception e) {
            logger.error("获取时间问候语失败", e);
        }
        
        return "大家好！我是" + robot.getNickname() + "，很高兴和大家聊天~";
    }
    
    @Override
    public String getContextualResponse(Robot robot, String contextType, Map<String, String> parameters) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> contextualConfig = getMapFromConfig(chatroomConfig, "contextual_responses");
            Map<String, Object> typeConfig = getMapFromConfig(contextualConfig, contextType);
            
            String template = (String) typeConfig.get("template");
            if (template != null) {
                Map<String, String> variables = new HashMap<>(parameters);
                variables.put("nickname", robot.getNickname());
                return processTemplate(template, variables);
            }
            
        } catch (Exception e) {
            logger.error("获取情境化回复失败: contextType={}", contextType, e);
        }
        
        return getFallbackResponse(robot);
    }
    
    @Override
    public String buildChatContextString(List<GroupChatMessage> messages, int maxLength) {
        if (messages == null || messages.isEmpty()) {
            return "这是一个新的聊天室，还没有对话历史。";
        }
        
        StringBuilder context = new StringBuilder();
        int currentLength = 0;
        
        // 按时间倒序遍历消息，构建上下文
        for (int i = messages.size() - 1; i >= 0 && currentLength < maxLength; i--) {
            GroupChatMessage message = messages.get(i);
            
            // 跳过系统消息
            if (Boolean.TRUE.equals(message.getIsSystemMessage())) {
                continue;
            }
            
            String senderName = message.getSenderNickname() != null ? 
                              message.getSenderNickname() : 
                              ("ROBOT".equals(message.getSenderType()) ? "机器人" : "用户");
            
            String messageLine = senderName + ": " + message.getContent() + "\n";
            
            if (currentLength + messageLine.length() > maxLength) {
                break;
            }
            
            context.insert(0, messageLine);
            currentLength += messageLine.length();
        }
        
        String result = context.toString().trim();
        logger.debug("构建的聊天上下文: 消息数量={}, 上下文长度={}, 内容={}", 
                   messages.size(), result.length(), 
                   result.isEmpty() ? "空" : result.substring(0, Math.min(200, result.length())));
        return result;
    }
    
    @Override
    public String getPersonalityTemplate(Robot robot, String templateType) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> personalityConfig = getMapFromConfig(chatroomConfig, "personality_templates");
            
            String personalityType = determinePersonalityType(robot);
            Map<String, Object> typeConfig = getMapFromConfig(personalityConfig, personalityType);
            
            String template = (String) typeConfig.get(templateType);
            if (template != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("nickname", robot.getNickname());
                return processTemplate(template, variables);
            }
            
        } catch (Exception e) {
            logger.error("获取个性模板失败: templateType={}", templateType, e);
        }
        
        return getFallbackResponse(robot);
    }
    
    @Override
    public String getTopicSuggestion(String topicType, Robot robot) {
        try {
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> topicConfig = getMapFromConfig(chatroomConfig, "topic_switch");
            Map<String, Object> suggestions = getMapFromConfig(topicConfig, "topic_suggestions");
            
            List<String> topicList = (List<String>) suggestions.get(topicType);
            if (topicList != null && !topicList.isEmpty()) {
                String suggestion = topicList.get(random.nextInt(topicList.size()));
                
                Map<String, String> variables = new HashMap<>();
                variables.put("nickname", robot.getNickname());
                variables.put("hobby", getRandomHobby());
                
                return processTemplate(suggestion, variables);
            }
            
        } catch (Exception e) {
            logger.error("获取话题建议失败: topicType={}", topicType, e);
        }
        
        return "大家想聊什么呢？";
    }
    
    @Override
    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        String result = template;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String key = matcher.group(1);
            String value = variables.get(key);
            
            if (value != null) {
                result = result.replace(placeholder, value);
            }
        }
        
        return result;
    }
    
    @Override
    public String getSystemMessage(String messageType, Map<String, String> parameters) {
        try {
            Map<String, Object> systemConfig = getMapFromConfig("system_messages");
            String template = (String) systemConfig.get(messageType);
            
            if (template != null) {
                return processTemplate(template, parameters);
            }
            
        } catch (Exception e) {
            logger.error("获取系统消息失败: messageType={}", messageType, e);
        }
        
        return "系统消息";
    }
    
    @Override
    public String getErrorHandlingResponse(String errorType) {
        try {
            Map<String, Object> errorConfig = getMapFromConfig("error_handling");
            List<String> responses = (List<String>) errorConfig.get(errorType);
            
            if (responses != null && !responses.isEmpty()) {
                return responses.get(random.nextInt(responses.size()));
            }
            
        } catch (Exception e) {
            logger.error("获取错误处理回复失败: errorType={}", errorType, e);
        }
        
        return "让我想想...";
    }
    
    @Override
    public boolean shouldSwitchTopic(List<GroupChatMessage> messages, int silenceDuration) {
        // 如果沉默时间超过10分钟，建议切换话题
        if (silenceDuration > 10) {
            return true;
        }
        
        // 如果最近5条消息都很短且单调，建议切换话题
        if (messages != null && messages.size() >= 5) {
            int shortMessageCount = 0;
            for (int i = messages.size() - 1; i >= messages.size() - 5; i--) {
                GroupChatMessage message = messages.get(i);
                if (message.getContent() != null && message.getContent().length() < 10) {
                    shortMessageCount++;
                }
            }
            return shortMessageCount >= 4;
        }
        
        return false;
    }
    
    @Override
    public String analyzeMessageSentimentAndGetStyle(String message, Robot robot) {
        // 简单的情感分析
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("开心") || lowerMessage.contains("高兴") || 
            lowerMessage.contains("哈哈") || lowerMessage.contains("😊")) {
            return "excited";
        } else if (lowerMessage.contains("难过") || lowerMessage.contains("伤心") || 
                  lowerMessage.contains("😢")) {
            return "sympathetic";
        } else if (lowerMessage.contains("？") || lowerMessage.contains("?")) {
            return "helpful";
        } else {
            return "friendly";
        }
    }
    
    // 辅助方法
    
    private Map<String, Object> getMapFromConfig(String key) {
        return (Map<String, Object>) promptConfig.get(key);
    }
    
    private Map<String, Object> getMapFromConfig(Map<String, Object> parent, String key) {
        return (Map<String, Object>) parent.get(key);
    }
    
    private String getPersonalityDescription(Robot robot) {
        if (robot.getPersonality() != null && !robot.getPersonality().trim().isEmpty()) {
            return robot.getPersonality();
        }
        return "友好、活泼、乐于助人";
    }
    
    private String getTimeSlot(LocalTime time) {
        if (time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(12, 0))) {
            return "morning";
        } else if (time.isAfter(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return "afternoon";
        } else if (time.isAfter(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(22, 0))) {
            return "evening";
        } else {
            return "night";
        }
    }
    
    private String determinePersonalityType(Robot robot) {
        String personality = robot.getPersonality();
        if (personality == null) {
            return "friendly";
        }
        
        personality = personality.toLowerCase();
        if (personality.contains("专业") || personality.contains("严谨")) {
            return "professional";
        } else if (personality.contains("幽默") || personality.contains("搞笑")) {
            return "humorous";
        } else if (personality.contains("知识") || personality.contains("学术")) {
            return "intellectual";
        } else {
            return "friendly";
        }
    }
    
    @Override
    public String buildChatPromptWithMemberInfo(Robot robot, String roomId, String chatContext) {
        try {
            // 获取聊天室成员信息
            String memberInfo = buildChatRoomMemberInfo(roomId);
            
            Map<String, Object> chatroomConfig = getMapFromConfig("chatroom");
            Map<String, Object> basicChatConfig = getMapFromConfig(chatroomConfig, "basic_chat");
            String template = (String) basicChatConfig.get("template");
            
            // 随机决定发送1-3条消息
            int messageCount = 1 + random.nextInt(3); // 1, 2, 或 3
            
            Map<String, String> variables = new HashMap<>();
            variables.put("nickname", robot.getNickname());
            variables.put("personality", getPersonalityDescription(robot));
            variables.put("chat_context", chatContext != null ? chatContext : "这是一个新的对话开始。");
            variables.put("member_info", memberInfo);
            variables.put("message_count", String.valueOf(messageCount));
            variables.put("split_instruction", getSplitInstruction(messageCount));
            
            // 如果模板不包含成员信息占位符，则在上下文前加入成员信息
            if (!template.contains("{member_info}")) {
                String enhancedContext = memberInfo + "\n\n" + (chatContext != null ? chatContext : "这是一个新的对话开始。");
                variables.put("chat_context", enhancedContext);
            }
            
            String result = processTemplate(template, variables);
            logger.debug("构建的包含成员信息的聊天提示词: robotId={}, messageCount={}, 长度={}", 
                       robot.getRobotId(), messageCount, result.length());
            return result;
            
        } catch (Exception e) {
            logger.error("构建包含成员信息的聊天提示词失败", e);
            return buildBasicChatPrompt(robot, chatContext);
        }
    }
    
    /**
     * 构建聊天室成员信息字符串
     */
    private String buildChatRoomMemberInfo(String roomId) {
        try {
            StringBuilder memberInfo = new StringBuilder();
            memberInfo.append("聊天室成员信息：\n");
            
            // 获取所有成员
            List<ChatRoomMember> members = memberService.getChatRoomMembers(roomId);
            
            // 分别处理用户和机器人
            List<ChatRoomMember> robotMembers = members.stream()
                    .filter(m -> "ROBOT".equals(m.getMemberType()))
                    .collect(Collectors.toList());
            
            List<ChatRoomMember> userMembers = members.stream()
                    .filter(m -> "USER".equals(m.getMemberType()))
                    .collect(Collectors.toList());
            
            // 添加机器人信息
            if (!robotMembers.isEmpty()) {
                memberInfo.append("机器人：");
                for (int i = 0; i < robotMembers.size(); i++) {
                    ChatRoomMember member = robotMembers.get(i);
                    Optional<Robot> robotOpt = robotRepository.findByRobotId(member.getMemberId());
                    if (robotOpt.isPresent()) {
                        Robot robot = robotOpt.get();
                        memberInfo.append(robot.getNickname())
                                .append("(")
                                .append(robot.getPersonality() != null ? robot.getPersonality() : "友好")
                                .append(")");
                        if (i < robotMembers.size() - 1) {
                            memberInfo.append("、");
                        }
                    }
                }
                memberInfo.append("\n");
            }
            
            // 添加用户信息（简化）
            if (!userMembers.isEmpty()) {
                memberInfo.append("用户：共").append(userMembers.size()).append("位\n");
            }
            
            return memberInfo.toString();
            
        } catch (Exception e) {
            logger.error("构建聊天室成员信息失败: roomId={}", roomId, e);
            return "聊天室成员信息获取失败\n";
        }
    }
    
    /**
     * 根据消息条数生成分隔指令
     */
    private String getSplitInstruction(int messageCount) {
        if (messageCount == 1) {
            return "直接发送一条消息即可。";
        } else {
            return String.format("如果要发%d条消息，请用\"|||\"分隔，例如：哈哈|||真的呀", messageCount);
        }
    }
    
    private String getRandomHobby() {
        String[] hobbies = {"读书", "电影", "音乐", "运动", "旅行", "摄影", "绘画", "编程"};
        return hobbies[random.nextInt(hobbies.length)];
    }
    
    private String getDefaultBasicChatPrompt(Robot robot, String chatContext) {
        return String.format(
            "你是%s，一个AI机器人伙伴。请用1-5句话自然地参与对话，保持友好的态度。当前对话：%s",
            robot.getNickname(), 
            chatContext != null ? chatContext : "这是新对话的开始。"
        );
    }
    
    private String getDefaultReplyPrompt(Robot robot, GroupChatMessage userMessage) {
        return String.format(
            "你是%s，请自然地回复%s的消息：%s", 
            robot.getNickname(),
            userMessage.getSenderNickname(),
            userMessage.getContent()
        );
    }
    
    private String getDefaultTopicSwitchPrompt(Robot robot) {
        return String.format(
            "你是%s，请主动提出一个有趣的话题来活跃聊天室的气氛。",
            robot.getNickname()
        );
    }
    
    private Map<String, Object> getDefaultConfig() {
        // 返回默认配置，避免配置文件加载失败时出错
        Map<String, Object> config = new HashMap<>();
        // 这里可以添加一些基本的默认配置
        logger.warn("使用默认提示词配置");
        return config;
    }
}