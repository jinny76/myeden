package com.myeden.service.impl;

import com.myeden.entity.ChatMessage;
import com.myeden.entity.Robot;
import com.myeden.model.external.WeatherInfo;
import com.myeden.repository.RobotRepository;
import com.myeden.service.AIChatService;
import com.myeden.service.ExternalDataCacheService;
import com.myeden.service.PromptService;
import com.myeden.service.DifyService;
import com.myeden.service.ChatService;
import com.myeden.service.UserRobotLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIChatServiceImpl implements AIChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIChatServiceImpl.class);
    
    @Autowired
    private RobotRepository robotRepository;
    @Autowired
    private PromptService promptService;

    @Autowired
    private ExternalDataCacheService externalDataCacheService;

    @Autowired
    private DifyService difyService;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserRobotLinkService userRobotLinkService;

    @Value("${dify.image.apiKey}")
    private String apiKey;

    @Value("${asr.server}")
    private String asrServerUrl;

    /**
     * 获取时间段描述
     */
    private String getTimeOfDay(LocalTime time) {
        if (time.isBefore(LocalTime.of(6, 0))) {
            return "夜深人静的时候";
        } else if (time.isBefore(LocalTime.of(9, 0))) {
            return "清晨时分";
        } else if (time.isBefore(LocalTime.of(12, 0))) {
            return "上午时光";
        } else if (time.isBefore(LocalTime.of(14, 0))) {
            return "午休时间";
        } else if (time.isBefore(LocalTime.of(18, 0))) {
            return "下午时光";
        } else if (time.isBefore(LocalTime.of(21, 0))) {
            return "傍晚时分";
        } else {
            return "夜晚时光";
        }
    }

    /**
     * 获取随机天气
     */
    private WeatherInfo getWeather(Robot robot) {
        // 获取缓存中的天气Map
        Map<String, WeatherInfo> weatherMap = externalDataCacheService.getWeatherMap();
        if (weatherMap != null) {
            String location = robot.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                WeatherInfo info = weatherMap.get(location.trim());
                if (info != null) {
                    // 找到对应城市天气
                    return info;
                }
            }

            // 未找到，随机返回一个已有城市的天气
            List<WeatherInfo> allWeather = new java.util.ArrayList<>(weatherMap.values());
            if (!allWeather.isEmpty()) {
                int idx = (int) (Math.random() * allWeather.size());
                return allWeather.get(idx);
            }
        }

        return null;
    }

    private String buildPostContext(Robot robot) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = now.toLocalTime();
        String weekDay = now.getDayOfWeek().toString();
        String timeOfDay = getTimeOfDay(time);
        WeatherInfo weatherInfo = getWeather(robot);
        String weather = weatherInfo != null ? weatherInfo.getDescription() : "未知";
        String temperature = weatherInfo != null ? weatherInfo.getTemperature() : "";
        weather = String.format("，天气%s, 温度%s", weather, temperature);

        return String.format(
                "现在是%s，%s，%s，%s",
                now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")),
                weekDay,
                timeOfDay,
                weather);
    }

    /**
     * 根据用户消息生成AI回复，参考生成评论的标准流程
     * @param userMessage 用户发送的消息
     * @return AI生成的回复消息
     */
    @Override
    public ChatMessage generateAIReply(ChatMessage userMessage) {
        String robotId = userMessage.getReceiverId();
        Optional<Robot> robotOpt = robotRepository.findByRobotId(robotId);
        Robot robot = robotOpt.orElse(null);
        if (robot == null) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(userMessage.getConversationId()); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent("很抱歉，机器人暂时无法回复。");
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        }
        try {
            // 直接调用PromptService统一生成AI回复

            if (userMessage.getImageBase64() != null && !userMessage.getImageBase64().isEmpty()) {
                // 调用Dify的图片识别接口
                DifyImageResult result = difyService.recognizeImageByWorkflow(userMessage.getImageBase64(), apiKey, userMessage.getSenderId(), "image");
                if (result.isSuccess()) {
                    userMessage.setContent(userMessage.getContent() + "\n(你在聊天视频窗口看到了：" + result.getText() + ")");
                }
            }

            if (userMessage.getAsrResult() != null && userMessage.getAsrResult().getEmotion() != null && !"NEUTRAL".equals(userMessage.getAsrResult().getEmotion())) {
                userMessage.setContent(userMessage.getContent() + ", 对方情绪是: " + userMessage.getAsrResult().getEmotion());
            }

            DifyService.DifyChatResult result = promptService.generateChatReply(robot, userMessage, buildPostContext(robot));
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(result.conversationId); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent(result.answer);
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        } catch (Exception e) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(userMessage.getSessionId());
            aiMsg.setConversationId(userMessage.getConversationId()); // 保持会话ID一致
            aiMsg.setSenderId(robotId);
            aiMsg.setSenderType("ai");
            aiMsg.setReceiverId(userMessage.getSenderId());
            aiMsg.setReceiverType("user");
            aiMsg.setContent("AI回复失败，请稍后再试。");
            aiMsg.setMsgType("text");
            aiMsg.setCreatedAt(LocalDateTime.now());
            aiMsg.setIsRead(false);
            return aiMsg;
        }
    }

    /**
     * 调用ASR服务识别音频（RestTemplate实现，无需新依赖）
     * @param audioFile 音频文件
     * @param key 文件名
     * @param lang 语言
     * @return 识别文本
     */
    private AIChatService.ASRRawTextInfo callASRService(File audioFile, String key, String lang) throws IOException {
        String url = asrServerUrl;
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        FileSystemResource fileResource = new FileSystemResource(audioFile);
        body.add("files", fileResource);
        body.add("keys", key);
        body.add("lang", lang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        String respStr = restTemplate.postForObject(url, requestEntity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(respStr);
        if (root.has("result") && root.get("result").isArray() && root.get("result").size() > 0) {
            JsonNode first = root.get("result").get(0);
            String rawText = first.get("raw_text").asText();
            // 解析结构化对象
            AIChatService.ASRRawTextInfo info = AIChatService.parseASRRawText(rawText);
            return info;
        }
        return null;
    }

    @Override
    public AIChatService.ASRRawTextInfo callASRService(File audioFile) {
        try {
            AIChatService.ASRRawTextInfo info = callASRService(audioFile, "voice", "zh");
            return info;
        } catch (Exception ex) {
            AIChatService.ASRRawTextInfo info = new AIChatService.ASRRawTextInfo();
            info.setText("[语音识别失败]");
            return info;
        } finally {
            if (audioFile != null && audioFile.exists()) {
                audioFile.delete();
            }
        }
    }
    
    @Override
    public boolean shouldInitiateProactiveChat(String userId, String robotId) {
        // 实现主动沟通判断逻辑
        // 这里可以根据熟悉度等级、时间间隔、用户在线状态等因素判断
        // 暂时返回false，避免编译错误
        return false;
    }
    
    @Override
    public CommunicationScore evaluateCommunicationQuality(List<ChatMessage> chatMessages) {
        // 实现沟通质量评估逻辑
        // 暂时返回一个默认的评分，避免编译错误
        return new CommunicationScore(5, "good", "正常沟通质量", 0);
    }
    
    @Override
    public ChatMessage generateFamiliarityBasedMessage(String userId, String robotId, Integer familiarityLevel) {
        // 实现基于熟悉度的消息生成逻辑
        // 暂时返回一个简单的消息，避免编译错误
        ChatMessage message = new ChatMessage();
        message.setSenderId(robotId);
        message.setSenderType("robot");
        message.setReceiverId(userId);
        message.setReceiverType("user");
        message.setContent("你好！很高兴和你聊天。");
        message.setMsgType("text");
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
    
    @Override
    public ChatMessage initiateProactiveChat(String userId, String robotId) {
        // 实现主动聊天发起逻辑
        // 暂时返回一个简单的主动消息，避免编译错误
        ChatMessage message = new ChatMessage();
        message.setSenderId(robotId);
        message.setSenderType("robot");
        message.setReceiverId(userId);
        message.setReceiverType("user");
        message.setContent("你好，我想和你聊聊天！");
        message.setMsgType("text");
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
    
    @Override
    public ChatMessage sendChatMessage(String userId, String robotId, String content, String imageBase64, String audioBase64, String conversationId) {
        try {
            // 获取机器人信息
            Optional<Robot> robotOpt = robotRepository.findByRobotId(robotId);
            if (robotOpt.isEmpty()) {
                logger.error("机器人不存在: {}", robotId);
                return null;
            }
            Robot robot = robotOpt.get();
            
            // 如果没有传入conversationId，说明是机器人主动发消息，需要建立会话上下文
            if (conversationId == null || conversationId.trim().isEmpty()) {
                try {
                    // 创建一个临时的用户消息用于建立会话上下文
                    ChatMessage contextMessage = new ChatMessage();
                    contextMessage.setSenderId(userId);
                    contextMessage.setSenderType("user");
                    contextMessage.setReceiverId(robotId);
                    contextMessage.setReceiverType("robot");
                    contextMessage.setContent("开始对话"); // 临时内容，用于建立上下文
                    contextMessage.setMsgType("text");
                    contextMessage.setConversationId(null); // 新会话
                    contextMessage.setCreatedAt(LocalDateTime.now());
                    
                    // 通过PromptService建立会话上下文，但使用机器人要发送的实际内容
                    String context = buildPostContext(robot);
                    
                    // 创建包含机器人主动消息内容的prompt
                    String proactivePrompt = String.format("你是%s，现在你想主动向用户发送消息：\"%s\"。请以自然的方式发送这条消息。", 
                                                         robot.getName(), content);
                    
                    // 临时替换消息内容为proactive prompt
                    contextMessage.setContent(proactivePrompt);
                    
                    // 调用PromptService生成AI回复以建立会话
                    DifyService.DifyChatResult result = promptService.generateChatReply(robot, contextMessage, context);
                    
                    if (result != null && result.conversationId != null) {
                        conversationId = result.conversationId;
                        // 使用AI生成的内容而不是原始content，这样更自然
                        content = result.answer;
                    }
                    
                } catch (Exception e) {
                    logger.warn("建立会话上下文失败，使用原始内容: {}", e.getMessage());
                    // 如果失败，使用原始内容继续
                }
            }
            
            // 创建聊天消息
            ChatMessage message = new ChatMessage();
            message.setSenderId(robotId);
            message.setSenderType("robot");
            message.setReceiverId(userId);
            message.setReceiverType("user");
            message.setContent(content);
            message.setMsgType("text");
            message.setConversationId(conversationId);
            message.setCreatedAt(LocalDateTime.now());
            // 标记为机器人主动触发的消息
            message.setIsProactiveMessage(true);
            
            // 保存消息到数据库
            chatService.sendMessage(message);
            
            // 设置用户与机器人链接的 hasPendingMessage 为 true
            userRobotLinkService.setPendingMessage(userId, robotId, true);
            
            return message;
        } catch (Exception e) {
            logger.error("发送聊天消息失败: {}", e.getMessage(), e);
            return null;
        }
    }
} 