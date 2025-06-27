package com.myeden.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web配置类
 * 负责RestTemplate、CORS、Web相关配置
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    /**
     * 配置ObjectMapper Bean
     * 用于JSON序列化和反序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 配置其他序列化选项
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        logger.info("ObjectMapper配置完成");
        return objectMapper;
    }

    /**
     * 配置RestTemplate Bean
     * 用于HTTP客户端请求，如调用Dify API
     */
    @Bean
    public RestTemplate restTemplate() {
        // 创建HTTP请求工厂
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间（30秒）
        factory.setConnectTimeout(30000);
        
        // 设置读取超时时间（60秒）
        factory.setReadTimeout(60000);
        
        // 创建RestTemplate
        RestTemplate restTemplate = new RestTemplate(factory);
        
        logger.info("RestTemplate配置完成，连接超时: 30s, 读取超时: 60s");
        return restTemplate;
    }

    /**
     * 配置CORS跨域
     * 注意：CORS配置源在SecurityConfig中定义，这里只配置WebMvc的CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        
        logger.info("WebMvc CORS跨域配置完成");
    }

    /**
     * 配置静态资源访问
     * 让上传的文件可以通过URL访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前工作目录的绝对路径
        String currentDir = System.getProperty("user.dir");
        String uploadPath = currentDir + "/uploads/";
        
        // 配置上传文件的静态资源访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCacheControl(org.springframework.http.CacheControl.noCache());
        
        logger.info("静态资源访问配置完成，上传文件可通过 /uploads/** 访问");
        logger.info("上传文件路径: {}", uploadPath);
    }

    /**
     * 创建带自定义配置的RestTemplate
     * 用于特定的API调用场景
     */
    @Bean("customRestTemplate")
    public RestTemplate customRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 更长的超时时间，适用于AI API调用
        factory.setConnectTimeout(60000);  // 60秒连接超时
        factory.setReadTimeout(120000);    // 120秒读取超时
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        logger.info("自定义RestTemplate配置完成，连接超时: 60s, 读取超时: 120s");
        return restTemplate;
    }

    /**
     * 创建用于文件上传的RestTemplate
     * 适用于文件传输场景
     */
    @Bean("fileRestTemplate")
    public RestTemplate fileRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 文件上传需要更长的超时时间
        factory.setConnectTimeout(30000);  // 30秒连接超时
        factory.setReadTimeout(300000);    // 5分钟读取超时
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        logger.info("文件上传RestTemplate配置完成，连接超时: 30s, 读取超时: 5min");
        return restTemplate;
    }
} 