package com.myeden.service.impl;

import com.myeden.entity.User;
import com.myeden.repository.UserRepository;
import com.myeden.repository.PostRepository;
import com.myeden.repository.CommentRepository;
import com.myeden.service.FileService;
import com.myeden.service.JwtService;
import com.myeden.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;

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
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    // 默认头像文件列表缓存
    private volatile List<String> defaultAvatarFiles = null;
    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000; // 5分钟缓存
    
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
        
        // 生成默认头像
        String defaultAvatar = generateDefaultAvatar();
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 创建用户
        User user = new User(userId, phone, encodedPassword);
        user.setNickname(nickname);
        user.setAvatar(defaultAvatar);
        user.setIsFirstLogin(true);
        
        // 保存用户
        userRepository.save(user);
        
        logger.info("新用户注册成功 - 用户ID: {}, 昵称: {}, 默认头像: {}", userId, nickname, defaultAvatar);
        
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
    
    @Override
    public UserPersonalStatistics getUserPersonalStatistics(String userId) {
        logger.info("获取用户个人统计信息，用户ID: {}", userId);
        
        // 获取用户基本信息
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 计算注册天数
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        Long registrationDays = ChronoUnit.DAYS.between(createdAt, now);
        
        // 获取今日开始时间
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // 统计发帖数
        Long totalPosts = postRepository.countByAuthorIdAndIsDeleted(userId, false);
        Long todayPosts = postRepository.countTodayPostsByAuthor(userId, startOfDay);
        
        // 统计评论数
        Long totalComments = commentRepository.countByAuthorIdAndIsDeleted(userId, false);
        Long todayComments = commentRepository.countTodayCommentsByAuthor(userId, startOfDay);
        
        // 统计获得的点赞数（动态点赞 + 评论点赞）
        Long totalPostLikes = calculateUserPostLikes(userId);
        Long totalCommentLikes = calculateUserCommentLikes(userId);
        Long totalLikes = totalPostLikes + totalCommentLikes;
        
        // 获取最后活跃时间（取最新的发帖或评论时间）
        String lastActiveTime = getLastActiveTime(userId);
        
        logger.info("用户个人统计信息计算完成 - 用户: {}, 注册天数: {}, 发帖数: {}, 评论数: {}, 总点赞数: {}", 
                   user.getNickname(), registrationDays, totalPosts, totalComments, totalLikes);
        
        return new UserPersonalStatistics(
            userId,
            user.getNickname(),
            user.getAvatar(),
            registrationDays,
            totalPosts,
            totalComments,
            totalLikes,
            totalPostLikes,
            totalCommentLikes,
            todayPosts,
            todayComments,
            lastActiveTime
        );
    }
    
    /**
     * 计算用户动态获得的点赞数
     * @param userId 用户ID
     * @return 点赞数
     */
    private Long calculateUserPostLikes(String userId) {
        try {
            // 获取用户的所有动态
            List<com.myeden.entity.Post> userPosts = postRepository.findByAuthorIdAndIsDeleted(userId, false);
            
            // 计算所有动态的点赞数总和
            return userPosts.stream()
                .mapToLong(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                .sum();
        } catch (Exception e) {
            logger.warn("计算用户动态点赞数失败，用户ID: {}", userId, e);
            return 0L;
        }
    }
    
    /**
     * 计算用户评论获得的点赞数
     * @param userId 用户ID
     * @return 点赞数
     */
    private Long calculateUserCommentLikes(String userId) {
        try {
            // 获取用户的所有评论
            List<com.myeden.entity.Comment> userComments = commentRepository.findByAuthorIdAndIsDeleted(userId, false);
            
            // 计算所有评论的点赞数总和
            return userComments.stream()
                .mapToLong(comment -> comment.getLikeCount() != null ? comment.getLikeCount() : 0)
                .sum();
        } catch (Exception e) {
            logger.warn("计算用户评论点赞数失败，用户ID: {}", userId, e);
            return 0L;
        }
    }
    
    /**
     * 获取用户最后活跃时间
     * @param userId 用户ID
     * @return 最后活跃时间字符串
     */
    private String getLastActiveTime(String userId) {
        try {
            LocalDateTime lastPostTime = null;
            LocalDateTime lastCommentTime = null;
            
            // 获取用户最新的发帖时间
            List<com.myeden.entity.Post> recentPosts = postRepository.findRecentPostsByAuthor(userId, 1);
            if (!recentPosts.isEmpty()) {
                lastPostTime = recentPosts.get(0).getCreatedAt();
            }
            
            // 获取用户最新的评论时间
            List<com.myeden.entity.Comment> recentComments = commentRepository.findRecentCommentsByAuthor(userId, 1);
            if (!recentComments.isEmpty()) {
                lastCommentTime = recentComments.get(0).getCreatedAt();
            }
            
            // 取较新的时间作为最后活跃时间
            LocalDateTime lastActiveTime = null;
            if (lastPostTime != null && lastCommentTime != null) {
                lastActiveTime = lastPostTime.isAfter(lastCommentTime) ? lastPostTime : lastCommentTime;
            } else if (lastPostTime != null) {
                lastActiveTime = lastPostTime;
            } else if (lastCommentTime != null) {
                lastActiveTime = lastCommentTime;
            } else {
                // 如果没有发帖和评论记录，使用注册时间
                Optional<User> userOpt = getUserById(userId);
                if (userOpt.isPresent()) {
                    lastActiveTime = userOpt.get().getCreatedAt();
                }
            }
            
            return lastActiveTime != null ? lastActiveTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
            
        } catch (Exception e) {
            logger.warn("获取用户最后活跃时间失败，用户ID: {}", userId, e);
            return "";
        }
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
        String[] nicknames = {
            "风起云涌", "小鹿乱撞", "橘子汽水", "星星点点", "爱吃甜的",
            "不知名路人", "懒癌晚期", "天天向上", "摸鱼选手", "随风而行",
            "爱喝奶茶", "人间清醒", "猫系青年", "不想长大", "自由如风",
            "吃货联盟", "一叶知秋", "慢慢来呀", "笑口常开", "岁月静好",
            "想太多啦", "生活不易", "认真生活", "偶尔疯一疯", "不争不抢",
            "云淡风轻", "随遇而安", "喜欢你啊", "做个梦吧", "别太认真",
            "简单就好", "慢慢变好", "温柔有光", "看透不说破", "自由自在",
            "想太多不如行动", "爱笑的人运气不会差", "生活需要一点甜", "偶尔emo一下", "做自己的光",
            "世界很大", "慢慢来不着急", "心若向阳", "生活明朗", "做个快乐的人",
            "不完美但真实", "每天都是新开始", "简单生活", "不慌不忙", "做个有温度的人",
            "世界那么大", "我想去看看", "生活不止眼前的苟且", "还有诗和远方", "做个有趣的人",
            "不被定义", "自由如风", "随心而行"
        };
        
        // 随机选择一个昵称
        String nickname = nicknames[(int) (Math.random() * nicknames.length)];
        
        // 如果昵称已存在，重新生成
        int retryCount = 0;
        while (existsByNickname(nickname) && retryCount < 10) {
            nickname = nicknames[(int) (Math.random() * nicknames.length)];
            retryCount++;
        }
        
        // 如果重试10次后仍然重复，在昵称后添加随机数字
        if (existsByNickname(nickname)) {
            int randomNum = (int) (Math.random() * 1000);
            nickname = nickname + randomNum;
        }
        
        return nickname;
    }
    
    /**
     * 生成默认头像URL
     * @return 默认头像URL
     */
    private String generateDefaultAvatar() {
        try {
            // 获取缓存的默认头像文件列表
            List<String> avatarFiles = getDefaultAvatarFiles();
            
            // 检查是否有可用的头像文件
            if (avatarFiles.isEmpty()) {
                logger.warn("没有找到可用的默认头像文件");
                return "/uploads/avatars/default.jpg"; // 返回默认头像
            }
            
            // 随机选择一个头像文件
            String selectedFile = avatarFiles.get((int) (Math.random() * avatarFiles.size()));
            
            // 返回头像URL路径
            String avatarUrl = "/uploads/avatars/users/" + selectedFile;
            logger.debug("随机选择默认头像: {}", avatarUrl);
            
            return avatarUrl;
            
        } catch (Exception e) {
            logger.error("生成默认头像时发生错误", e);
            return "/uploads/avatars/default.jpg"; // 返回默认头像
        }
    }
    
    /**
     * 获取默认头像文件列表（带缓存）
     * @return 默认头像文件列表
     */
    private List<String> getDefaultAvatarFiles() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (defaultAvatarFiles != null && (currentTime - lastCacheUpdate) < CACHE_DURATION) {
            return defaultAvatarFiles;
        }
        
        // 重新扫描目录
        synchronized (this) {
            // 双重检查锁定
            if (defaultAvatarFiles != null && (currentTime - lastCacheUpdate) < CACHE_DURATION) {
                return defaultAvatarFiles;
            }
            
            List<String> newAvatarFiles = new CopyOnWriteArrayList<>();
            
            try {
                // 获取默认头像目录路径
                String avatarDirPath = "uploads/avatars/users";
                File avatarDir = new File(avatarDirPath);
                
                // 检查目录是否存在
                if (!avatarDir.exists() || !avatarDir.isDirectory()) {
                    logger.warn("默认头像目录不存在: {}", avatarDirPath);
                    defaultAvatarFiles = newAvatarFiles;
                    lastCacheUpdate = currentTime;
                    return newAvatarFiles;
                }
                
                // 获取目录中的所有文件
                File[] files = avatarDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                           lowerName.endsWith(".png") || lowerName.endsWith(".gif");
                });
                
                // 收集文件名
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            newAvatarFiles.add(file.getName());
                        }
                    }
                }
                
                logger.info("扫描到 {} 个默认头像文件", newAvatarFiles.size());
                
            } catch (Exception e) {
                logger.error("扫描默认头像目录时发生错误", e);
            }
            
            // 更新缓存
            defaultAvatarFiles = newAvatarFiles;
            lastCacheUpdate = currentTime;
            
            return newAvatarFiles;
        }
    }
} 