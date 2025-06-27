package com.myeden.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.entity.User;
import com.myeden.repository.UserRepository;
import com.myeden.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器集成测试
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 清理测试数据
        userRepository.deleteAll();
        
        // 创建测试用户
        testUser = new User();
        testUser.setUserId("test-user-001");
        testUser.setPhone("13800138000");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setNickname("测试用户");
        testUser.setAvatar("/avatars/default.jpg");
        testUser.setTitle("测试者");
        testUser.setGender("男");
        testUser.setAge(25);
        testUser.setBirthday(LocalDate.now().minusYears(25));
        testUser.setIntroduction("这是一个测试用户");
        testUser.setBackground("测试背景");
        testUser.setBloodType("A");
        testUser.setMbti("INTJ");
        testUser.setFavoriteColor("蓝色");
        testUser.setLikes(java.util.Arrays.asList("编程", "音乐", "旅行"));
        testUser.setDislikes(java.util.Arrays.asList("噪音", "拥挤"));
        testUser.setIsFirstLogin(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(testUser);
        
        // 生成认证Token
        authToken = jwtService.generateToken(testUser.getUserId());
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        // 准备测试数据
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", "13800138001");
        requestBody.put("password", "password123");

        // 执行测试
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.nickname").exists());
    }

    @Test
    void testRegister_PhoneAlreadyExists() throws Exception {
        // 准备测试数据
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", "13800138000"); // 已存在的手机号
        requestBody.put("password", "password123");

        // 执行测试
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLogin_Success() throws Exception {
        // 准备测试数据
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", "13800138000");
        requestBody.put("password", "password123");

        // 执行测试
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.userId").value("test-user-001"))
                .andExpect(jsonPath("$.data.user.phone").value("13800138000"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // 准备测试数据
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", "13800138000");
        requestBody.put("password", "wrongpassword");

        // 执行测试
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetUserProfile_Success() throws Exception {
        // 执行测试 - 使用实际的API端点 /me
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value("test-user-001"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    void testGetUserProfile_Unauthorized() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateUserProfile_Success() throws Exception {
        // 准备测试数据
        User updateData = new User();
        updateData.setNickname("新昵称");
        updateData.setIntroduction("新介绍");
        updateData.setAge(26);

        // 执行测试 - 使用实际的API端点 /{userId}
        mockMvc.perform(put("/api/v1/users/{userId}", "test-user-001")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.introduction").value("新介绍"))
                .andExpect(jsonPath("$.data.age").value(26));
    }

    @Test
    void testUpdateUserProfile_Unauthorized() throws Exception {
        // 准备测试数据
        User updateData = new User();
        updateData.setNickname("新昵称");

        // 执行测试
        mockMvc.perform(put("/api/v1/users/{userId}", "test-user-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUploadAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", // 参数名
            "test-avatar.jpg", // 文件名
            "image/jpeg", // 媒体类型
            "fake image content".getBytes() // 文件内容
        );

        mockMvc.perform(multipart("/api/v1/users/{userId}/avatar", "test-user-001")
                .file(file)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.avatarUrl").exists());
    }

    @Test
    void testUploadAvatar_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-avatar.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/users/{userId}/avatar", "test-user-001")
                .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/v1/users/{userId}", "test-user-001")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value("test-user-001"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/v1/users/{userId}", "non-existent-user")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void testGetUserById_Unauthorized() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/v1/users/{userId}", "test-user-001"))
                .andExpect(status().isOk()); // 这个API不需要认证
    }
} 