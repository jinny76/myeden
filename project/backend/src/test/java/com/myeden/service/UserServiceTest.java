package com.myeden.service;

import com.myeden.entity.User;
import com.myeden.repository.UserRepository;
import com.myeden.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("test-user-001");
        testUser.setPhone("13800138000");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setAvatar("/avatars/default.jpg");
        testUser.setTitle("测试者");
        testUser.setGender("男");
        testUser.setAge(25);
        testUser.setBirthday(LocalDateTime.now().minusYears(25));
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
    }

    @Test
    void testRegister_Success() {
        // 准备测试数据
        String phone = "13800138000";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        String token = "jwt-token-123";

        // Mock行为
        when(userRepository.existsByPhone(phone)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString())).thenReturn(token);

        // 执行测试
        UserService.UserRegisterResult result = userService.register(phone, password);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getUserId());
        assertNotNull(result.getNickname());
        assertEquals(token, result.getToken());

        // 验证方法调用
        verify(userRepository).existsByPhone(phone);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyString());
    }

    @Test
    void testRegister_PhoneAlreadyExists() {
        // 准备测试数据
        String phone = "13800138000";
        String password = "password123";

        // Mock行为
        when(userRepository.existsByPhone(phone)).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.register(phone, password);
        });

        // 验证方法调用
        verify(userRepository).existsByPhone(phone);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserByPhone_Success() {
        // 准备测试数据
        String phone = "13800138000";

        // Mock行为
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(testUser));

        // 执行测试
        Optional<User> result = userService.getUserByPhone(phone);

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(phone, result.get().getPhone());

        // 验证方法调用
        verify(userRepository).findByPhone(phone);
    }

    @Test
    void testGetUserByPhone_NotFound() {
        // 准备测试数据
        String phone = "13800138000";

        // Mock行为
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        // 执行测试
        Optional<User> result = userService.getUserByPhone(phone);

        // 验证结果
        assertFalse(result.isPresent());

        // 验证方法调用
        verify(userRepository).findByPhone(phone);
    }

    @Test
    void testGetUserById_Success() {
        // 准备测试数据
        String userId = "test-user-001";

        // Mock行为
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));

        // 执行测试
        Optional<User> result = userService.getUserById(userId);

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());

        // 验证方法调用
        verify(userRepository).findByUserId(userId);
    }

    @Test
    void testUpdateUser_Success() {
        // 准备测试数据
        String userId = "test-user-001";
        User updateData = new User();
        updateData.setNickname("新昵称");
        updateData.setIntroduction("新介绍");

        // Mock行为
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        User result = userService.updateUser(userId, updateData);

        // 验证结果
        assertNotNull(result);

        // 验证方法调用
        verify(userRepository).findByUserId(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // 准备测试数据
        String userId = "non-existent-user";
        User updateData = new User();

        // Mock行为
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userId, updateData);
        });

        // 验证方法调用
        verify(userRepository).findByUserId(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCompleteFirstLogin_Success() {
        // 准备测试数据
        String userId = "test-user-001";

        // Mock行为
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        userService.completeFirstLogin(userId);

        // 验证方法调用
        verify(userRepository).findByUserId(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testExistsByPhone_True() {
        // 准备测试数据
        String phone = "13800138000";

        // Mock行为
        when(userRepository.existsByPhone(phone)).thenReturn(true);

        // 执行测试
        boolean result = userService.existsByPhone(phone);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(userRepository).existsByPhone(phone);
    }

    @Test
    void testExistsByPhone_False() {
        // 准备测试数据
        String phone = "13800138000";

        // Mock行为
        when(userRepository.existsByPhone(phone)).thenReturn(false);

        // 执行测试
        boolean result = userService.existsByPhone(phone);

        // 验证结果
        assertFalse(result);

        // 验证方法调用
        verify(userRepository).existsByPhone(phone);
    }
} 