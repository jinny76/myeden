package com.myeden.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 测试配置类
 * 为测试环境提供必要的Bean配置
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@TestConfiguration
public class TestConfig {

    /**
     * 测试环境ObjectMapper配置
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    /**
     * 测试环境RestTemplate配置
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 测试环境使用较短的超时时间
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    /**
     * 测试环境自定义RestTemplate
     */
    @Bean("customRestTemplate")
    public RestTemplate customRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    /**
     * 测试环境文件上传RestTemplate
     */
    @Bean("fileRestTemplate")
    public RestTemplate fileRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }
} 