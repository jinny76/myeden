package com.myeden.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * TTSController
 * 支持文字转语音，指定声音名称，生成音频文件并返回URL。
 * 配置读取自resources/config/api.json，accessToken以JWT方式放在头部。
 */
@RestController
@RequestMapping("/api/v1/tts")
public class TTSController {

    // 配置项
    private String ttsUrl;
    private String appId;
    private String accessToken;
    private String userId;
    private String secretKey;

    @Value("${file.upload.path:./uploads}")
    private String uploadBasePath;

    /**
     * 启动时加载TTS配置
     */
    @PostConstruct
    public void loadConfig() throws IOException {
        // 读取api.json配置
        ClassPathResource resource = new ClassPathResource("config/api.json");
        String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode tts = root.path("voice").path("tts");
        ttsUrl = tts.path("url").asText();
        appId = tts.path("appId").asText();
        accessToken = tts.path("accessToken").asText();
        userId = tts.path("userId").asText();
        secretKey = tts.path("secretKey").asText();
    }

    /**
     * TTS合成接口
     * @param reqBody 请求体，包含text、voice_type等
     * @return 返回音频文件URL和时长
     */
    @PostMapping
    public ResponseEntity<?> tts(@RequestBody Map<String, Object> reqBody) {
        try {
            // 1. 组装TTS请求体
            Map<String, Object> ttsReq = new HashMap<>();
            Map<String, Object> app = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            Map<String, Object> audio = new HashMap<>();
            Map<String, Object> request = new HashMap<>();

            // 解析参数
            String text = ((Map<String, Object>) reqBody.getOrDefault("request", Collections.emptyMap())).getOrDefault("text", "").toString();
            String voiceType = ((Map<String, Object>) reqBody.getOrDefault("audio", Collections.emptyMap())).getOrDefault("voice_type", "zh_male_M392_conversation_wvae_bigtts").toString();
            String encoding = ((Map<String, Object>) reqBody.getOrDefault("audio", Collections.emptyMap())).getOrDefault("encoding", "mp3").toString();
            double speed = Double.parseDouble(((Map<String, Object>) reqBody.getOrDefault("audio", Collections.emptyMap())).getOrDefault("speed_ratio", "1.0").toString());
            String reqid = UUID.randomUUID().toString();

            String key = voiceType + text + encoding;
            // md5 hash
            String fileName = DigestUtils.md5DigestAsHex(key.getBytes()) + "." + encoding;    

            // 如果文件存在，则返回文件URL
            String filePath = uploadBasePath + "/voices/" + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("url", filePath);
                return ResponseEntity.ok(new EventResponse(
                        200,
                        "生成成功", result));
            }
            
            // app部分
            app.put("appid", appId);
            app.put("token", accessToken);
            app.put("cluster", "volcano_tts");
            // user部分
            user.put("uid", userId);
            // audio部分
            audio.put("voice_type", voiceType);
            audio.put("encoding", encoding);
            audio.put("speed_ratio", speed);
            // request部分
            request.put("reqid", reqid);
            request.put("text", text);
            request.put("operation", "query");

            ttsReq.put("app", app);
            ttsReq.put("user", user);
            ttsReq.put("audio", audio);
            ttsReq.put("request", request);

            // 2. 发送TTS请求
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer;" + accessToken); // JWT方式
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ttsReq, headers);

            ResponseEntity<String> ttsResp = restTemplate.postForEntity(ttsUrl, entity, String.class);

            // 3. 解析TTS响应
            ObjectMapper mapper = new ObjectMapper();
            JsonNode respNode = mapper.readTree(ttsResp.getBody());
            int code = respNode.path("code").asInt();
            if (code != 3000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respNode);
            }
            String base64Audio = respNode.path("data").asText();
            byte[] audioBytes = Base64.getDecoder().decode(base64Audio);

            // 4. 保存到uploads/voices/
            String voicesDir = uploadBasePath + "/voices/";
            File dir = new File(voicesDir);
            if (!dir.exists()) dir.mkdirs();
                        
            File audioFile = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioBytes);
            }
            // 5. 返回URL字符串
            String url = "/uploads/voices/" + fileName;
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("duration", respNode.path("addition").path("duration").asText(""));
            return ResponseEntity.ok(new EventResponse(
                200,
                "生成成功",
                result
            ));
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "TTS合成失败");
            err.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
} 