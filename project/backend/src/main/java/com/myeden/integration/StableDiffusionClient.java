package com.myeden.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Stable Diffusion API客户端
 * 用于生成图片
 */
@Component
public class StableDiffusionClient {
    
    private static final Logger logger = LoggerFactory.getLogger(StableDiffusionClient.class);
    
    @Value("${sd.api.url:http://localhost:7860}")
    private String apiUrl;
    
    @Value("${sd.api.timeout:30000}")
    private int timeout;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public StableDiffusionClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
                    /**
                 * 生成图片
                 * @param prompt 提示词
                 * @return 生成的图片数据（Base64编码），失败返回null
                 */
                public byte[] generateImage(String prompt) {
                    try {
                        logger.info("开始生成图片: prompt={}", 
                            prompt.substring(0, Math.min(prompt.length(), 100)));
                        
                        // 构建请求体 - 使用指定的SD参数
                        Map<String, Object> requestBody = new HashMap<>();
                        requestBody.put("prompt", prompt);
                        requestBody.put("negative_prompt", "EasyNegative, paintings, sketches, (worst quality:2), (low quality:2), (normal quality:2), lowres, normal quality, ((monochrome)), ((grayscale)), skin spots, acnes, skin blemishes, age spot, glans,extra fingers,fewer fingers,strange fingers,bad hand,backlight, (worst quality, low quality:1.4), watermark, logo, bad anatomy,lace,rabbit,back,");
                        requestBody.put("height", 1080);
                        requestBody.put("width", 1920);
                        requestBody.put("sampler_index", "Euler");
                        requestBody.put("scheduler", "Simple");
                        requestBody.put("cfg_scale", 1);
                        requestBody.put("distilled_cfg_scale", 3.5);
                        requestBody.put("seed", -1);
                        requestBody.put("steps", 20);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/sdapi/v1/txt2img", 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                byte[] imageData = extractImageData(responseJson);
                
                if (imageData != null && imageData.length > 0) {
                    logger.info("图片生成成功: size={} bytes", imageData.length);
                    return imageData;
                } else {
                    logger.warn("生成的图片数据为空");
                    return null;
                }
            } else {
                logger.error("SD API调用失败: status={}, body={}", 
                    response.getStatusCode(), response.getBody());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("生成图片时发生异常: error={}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从响应中提取图片数据
     */
    private byte[] extractImageData(JsonNode responseJson) {
        try {
            // 根据SD API的实际响应格式提取图片数据
            if (responseJson.has("images") && responseJson.get("images").isArray() && 
                responseJson.get("images").size() > 0) {
                
                String base64Image = responseJson.get("images").get(0).asText();
                return Base64.getDecoder().decode(base64Image);
            }
            return null;
        } catch (Exception e) {
            logger.error("提取图片数据失败: error={}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 检查SD服务是否可用
     */
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl + "/sdapi/v1/progress", String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("SD服务不可用: error={}", e.getMessage());
            return false;
        }
    }
} 