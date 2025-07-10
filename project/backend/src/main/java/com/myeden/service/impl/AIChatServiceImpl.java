package com.myeden.service.impl;

import com.myeden.entity.ChatMessage;
import com.myeden.entity.Robot;
import com.myeden.model.external.WeatherInfo;
import com.myeden.repository.RobotRepository;
import com.myeden.service.AIChatService;
import com.myeden.service.ExternalDataCacheService;
import com.myeden.service.PromptService;
import com.myeden.service.DifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AIChatServiceImpl implements AIChatService {
    @Autowired
    private RobotRepository robotRepository;
    @Autowired
    private PromptService promptService;

    @Autowired
    private ExternalDataCacheService externalDataCacheService;

    @Autowired
    private DifyService difyService;

    @Value("${dify.image.apiKey}")
    private String apiKey;

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
} 