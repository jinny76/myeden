package com.myeden.service.impl;

import com.myeden.entity.User;
import com.myeden.repository.UserRepository;
import com.myeden.service.FileService;
import com.myeden.service.JwtService;
import com.myeden.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户管理服务实现类
 * 
 * 功能说明：
 * - 实现用户注册、登录、信息管理等核心业务功能
 * - 支持用户头像上传和个人资料更新
 * - 提供用户查询和统计功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private FileService fileService;
    
    @Override
    public UserRegisterResult register(String phone, String password) {
        // 检查手机号是否已存在
        if (existsByPhone(phone)) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 生成用户ID
        String userId = generateUserId();
        
        // 生成昵称
        String nickname = generateNickname();
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 创建用户
        User user = new User(userId, phone, encodedPassword);
        user.setNickname(nickname);
        user.setIsFirstLogin(true);
        
        // 保存用户
        userRepository.save(user);
        
        // 生成JWT token
        String token = jwtService.generateToken(userId);
        
        return new UserRegisterResult(userId, nickname, token);
    }
    
    @Override
    public UserLoginResult login(String phone, String password) {
        // 根据手机号查找用户
        Optional<User> userOpt = getUserByPhone(phone);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成JWT token
        String token = jwtService.generateToken(user.getUserId());
        
        return new UserLoginResult(user.getUserId(), token, user.getIsFirstLogin(), user);
    }
    
    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    @Override
    public User updateUser(String userId, User userUpdate) {
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 更新用户信息
        user.updateUser(userUpdate);
        
        // 保存用户
        return userRepository.save(user);
    }
    
    @Override
    public String uploadAvatar(String userId, MultipartFile file) {
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 上传文件
        String avatarUrl = fileService.uploadFile(file, "avatars");
        
        // 更新用户头像
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        
        return avatarUrl;
    }
    
    @Override
    public void completeFirstLogin(String userId) {
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.completeFirstLogin();
            userRepository.save(user);
        }
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
    
    @Override
    public List<User> searchUsersByNickname(String nickname, int limit) {
        return userRepository.findByNicknameContaining(nickname);
    }
    
    @Override
    public List<User> getRecentUsers(int limit) {
        return userRepository.findRecentUsers(limit);
    }
    
    @Override
    public UserStatistics getStatistics() {
        // 获取今日开始时间
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // 统计各项数据
        Long totalUsers = userRepository.count();
        Long todayRegisteredUsers = (long) userRepository.findTodayRegisteredUsers(startOfDay).size();
        Long maleUsers = userRepository.countByGender("male");
        Long femaleUsers = userRepository.countByGender("female");
        Long firstLoginUsers = userRepository.countByIsFirstLogin(true);
        
        return new UserStatistics(totalUsers, todayRegisteredUsers, maleUsers, femaleUsers, firstLoginUsers);
    }
    
    /**
     * 生成用户ID
     * @return 用户ID
     */
    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 生成昵称
     * @return 昵称
     */
    private String generateNickname() {
        String[] prefixes = {"用户", "玩家", "朋友", "伙伴", "小伙伴"};
        String[] suffixes = {"001", "002", "003", "004", "005", "006", "007", "008", "009", "010"};
        
        String prefix = prefixes[(int) (Math.random() * prefixes.length)];
        String suffix = suffixes[(int) (Math.random() * suffixes.length)];
        
        String nickname = prefix + suffix;
        
        // 如果昵称已存在，重新生成
        int retryCount = 0;
        while (existsByNickname(nickname) && retryCount < 10) {
            suffix = String.valueOf((int) (Math.random() * 1000));
            nickname = prefix + suffix;
            retryCount++;
        }
        
        return nickname;
    }
} 