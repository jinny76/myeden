package com.myeden.controller;

import com.myeden.entity.ChatMessage;
import com.myeden.model.WebSocketMessage;
import com.myeden.service.ChatService;
import com.myeden.service.WebSocketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import com.myeden.service.AIChatService;
import com.myeden.service.UserRobotLinkService;
import com.myeden.controller.EventResponse;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.myeden.service.ExpertMemoryService;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private AIChatService aiChatService;
    @Autowired
    private UserRobotLinkService userRobotLinkService;
    @Autowired
    private ExpertMemoryService expertMemoryService;
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    /**
     * 将base64音频字符串保存为本地文件
     * @param base64Str base64字符串（可带data:audio/wav;base64,前缀）
     * @param savePath 保存路径
     * @return File对象
     */
    private File saveBase64AudioToFile(String base64Str, String savePath) throws IOException {
        String base64 = base64Str;
        if (base64.contains(",")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }
        byte[] data = Base64.getDecoder().decode(base64);
        File file = new File(savePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file;
    }

    /**
     * 发送聊天消息，并推送给接收方
     */
    @PostMapping("/send")
    public ResponseEntity<EventResponse> sendMessage(@RequestBody ChatMessage message) {
        try {
            if (message.getAudioBase64() != null && !message.getAudioBase64().isEmpty()) {
                // 1. 保存音频到本地文件
                File audioFile = File.createTempFile("audio", ".webm");
                try {
                    saveBase64AudioToFile(message.getAudioBase64(), audioFile.getAbsolutePath());

                    // 2. 将音频文件转换为wav格式
                    File wavFile = convertAudioToWav(audioFile, StringUtils.replace(audioFile.getAbsolutePath(), ".webm", ".wav"));

                    // 2. 调用ASR服务识别音频内容
                    AIChatService.ASRRawTextInfo asrText = aiChatService.callASRService(wavFile);
                    // 3. 识别结果写入消息内容
                    message.setContent(asrText.getText());
                    message.setAsrResult(asrText);
                } catch (Exception ex) {
                    // 识别失败，写入提示
                    message.setContent("[语音识别失败]");
                } finally {
                    // 可选：删除临时文件
                    if (audioFile != null && audioFile.exists()) {
                        audioFile.delete();
                    }
                }
            }
                

            ChatMessage chatMessage = new ChatMessage();
            // clone message for save
            chatMessage.setSessionId(message.getSessionId());
            chatMessage.setConversationId(message.getConversationId());
            chatMessage.setSenderId(message.getSenderId());
            chatMessage.setSenderType(message.getSenderType());
            chatMessage.setReceiverId(message.getReceiverId());
            chatMessage.setReceiverType(message.getReceiverType());
            chatMessage.setContent(message.getContent());
            chatMessage.setMsgType(message.getMsgType());
            chatMessage.setCreatedAt(message.getCreatedAt());
            chatMessage.setIsRead(message.getIsRead());
            chatMessage.setIsDeleted(message.getIsDeleted());
            chatMessage.setAsrResult(message.getAsrResult());
            chatMessage.setExpertThemeId(message.getExpertThemeId());
            chatMessage.setSessionType(message.getSessionType());

            // 1. 保存用户消息
            chatService.sendMessage(chatMessage);

            // 2. 推送给接收方
            WebSocketMessage<ChatMessage> wsMsg = WebSocketMessage.chat(chatMessage);
            webSocketService.sendMessageToUser(chatMessage.getSenderId(), wsMsg);

            // 3. 增加用户与机器人熟悉度积分（如果是用户对机器人说话）
            if ("user".equals(message.getSenderType()) && 
                ("robot".equalsIgnoreCase(message.getReceiverType()) || isRobotId(message.getReceiverId()))) {
                try {
                    boolean familiarityUpdated = userRobotLinkService.addFamiliarityScoreByAction(
                        message.getSenderId(), message.getReceiverId(), "chat");
                    if (familiarityUpdated) {
                        logger.info("用户{}与机器人{}聊天熟悉度积分已更新", message.getSenderId(), message.getReceiverId());
                    }
                } catch (Exception e) {
                    logger.warn("更新聊天熟悉度积分失败，用户ID: {}, 机器人ID: {}", 
                        message.getSenderId(), message.getReceiverId(), e);
                }
            }

            // 4. 检查是否需要AI回复，异步处理（手动新建线程）
            if ("robot".equalsIgnoreCase(message.getReceiverType()) || isRobotId(message.getReceiverId())) {
                new Thread(() -> aiReplyAsync(message)).start();
            }

            // 5. 立即返回，仅包含用户消息
            return ResponseEntity.ok(new EventResponse(200, "消息发送成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "消息发送失败: " + e.getMessage(), null));
        }
    }

    private File convertAudioToWav(File audioFile, String wavFileName) throws IOException {
        // 使用FFmpeg将webm音频文件转换为wav格式
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegPath, "-i", audioFile.getAbsolutePath(), wavFileName);
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();

            // 检查转换结果
            File wavFile = new File(wavFileName);
            if (!wavFile.exists() || wavFile.length() == 0) {
                throw new IOException("转换后的音频文件不存在或为空");
            }

            // 检查转换后的文件是否为wav格式
            if (!wavFile.getName().toLowerCase().endsWith(".wav")) {
                throw new IOException("转换后的音频文件不是wav格式");
            }
            return wavFile;
        } catch (Exception e) {
            throw new IOException("转换音频文件失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成AI回复并推送（不再依赖@Async）
     * @param userMessage 用户发送的消息
     */
    public void aiReplyAsync(ChatMessage userMessage) {
        try {
            // 1. 生成AI回复
            ChatMessage aiReply = aiChatService.generateAIReply(userMessage);
            // 2. 保存AI回复
            chatService.sendMessage(aiReply);
            // 3. 推送AI回复
            WebSocketMessage<ChatMessage> aiWsMsg = WebSocketMessage.chat(aiReply);
            webSocketService.sendMessageToUser(aiReply.getReceiverId(), aiWsMsg);
        } catch (Exception e) {
            // 可记录日志，便于排查AI回复异常
            org.slf4j.LoggerFactory.getLogger(ChatController.class).error("AI回复生成失败", e);
        }
    }

    // 判断是否为AI机器人ID（可根据实际业务调整）
    private boolean isRobotId(String receiverId) {
        return receiverId != null && receiverId.startsWith("robot_");
    }

    /**
     * 查询会话历史消息（按sessionId）
     */
    @GetMapping("/history")
    public ResponseEntity<EventResponse> getHistoryBySession(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            List<ChatMessage> history = chatService.getHistoryBySession(sessionId, limit, offset);
            return ResponseEntity.ok(new EventResponse(200, "获取历史消息成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取历史消息失败: " + e.getMessage(), null));
        }
    }

    /**
     * 查询与指定机器人所有历史消息（游标分页，desc排序，前端reverse）
     */
    @GetMapping("/history/robot")
    public ResponseEntity<EventResponse> getHistoryWithRobot(
            @RequestParam String userId,
            @RequestParam String robotId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String before, // ISO时间字符串
            @RequestParam(required = false) String expertThemeId // 专家主题ID，null表示随便聊聊模式
    ) {
        try {
            List<ChatMessage> history;

            String themeId = "null".equals(expertThemeId) ? null : expertThemeId;
            if (before != null && !before.isEmpty()) {
                java.time.LocalDateTime beforeTime = java.time.LocalDateTime.parse(before);
                history = chatService.getHistoryWithRobotByExpertThemeBefore(userId, robotId, themeId, beforeTime, limit);
            } else {
                history = chatService.getLatestHistoryWithRobotByExpertTheme(userId, robotId, themeId, limit);
            }
            // 不再排序，直接返回desc，前端reverse
            return ResponseEntity.ok(new EventResponse(200, "获取历史消息成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取历史消息失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据conversationId查询该会话的所有消息（按创建时间升序）
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<EventResponse> getHistoryByConversationId(@PathVariable String conversationId) {
        try {
            List<ChatMessage> history = chatService.getHistoryByConversationId(conversationId);
            return ResponseEntity.ok(new EventResponse(200, "获取会话历史消息成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取会话历史消息失败: " + e.getMessage(), null));
        }
    }
} 