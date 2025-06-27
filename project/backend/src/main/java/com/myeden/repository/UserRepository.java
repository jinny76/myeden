package com.myeden.repository;

import com.myeden.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * 功能说明：
 * - 提供用户相关的数据库操作
 * - 支持按手机号、用户ID等字段查询
 * - 提供用户统计和状态查询
 * - 支持用户数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户信息
     */
    Optional<User> findByUserId(String userId);
    
    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据昵称查找用户
     * @param nickname 昵称
     * @return 用户信息
     */
    Optional<User> findByNickname(String nickname);
    
    /**
     * 检查用户ID是否存在
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(String userId);
    
    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 检查昵称是否存在
     * @param nickname 昵称
     * @return 是否存在
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 根据性别查找用户
     * @param gender 性别
     * @return 用户列表
     */
    List<User> findByGender(String gender);
    
    /**
     * 根据年龄范围查找用户
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    @Query("{'age': {$gte: ?0, $lte: ?1}}")
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);
    
    /**
     * 根据MBTI查找用户
     * @param mbti MBTI类型
     * @return 用户列表
     */
    List<User> findByMbti(String mbti);
    
    /**
     * 根据血型查找用户
     * @param bloodType 血型
     * @return 用户列表
     */
    List<User> findByBloodType(String bloodType);
    
    /**
     * 根据是否首次登录查找用户
     * @param isFirstLogin 是否首次登录
     * @return 用户列表
     */
    List<User> findByIsFirstLogin(Boolean isFirstLogin);
    
    /**
     * 查找最近注册的用户
     * @param limit 限制数量
     * @return 用户列表
     */
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<User> findRecentUsers(int limit);
    
    /**
     * 根据昵称模糊查询用户
     * @param nickname 昵称关键词
     * @return 用户列表
     */
    @Query("{'nickname': {$regex: ?0, $options: 'i'}}")
    List<User> findByNicknameContaining(String nickname);
    
    /**
     * 根据介绍模糊查询用户
     * @param introduction 介绍关键词
     * @return 用户列表
     */
    @Query("{'introduction': {$regex: ?0, $options: 'i'}}")
    List<User> findByIntroductionContaining(String introduction);
    
    /**
     * 根据背景模糊查询用户
     * @param background 背景关键词
     * @return 用户列表
     */
    @Query("{'background': {$regex: ?0, $options: 'i'}}")
    List<User> findByBackgroundContaining(String background);
    
    /**
     * 根据喜欢的东西查找用户
     * @param like 喜欢的东西
     * @return 用户列表
     */
    @Query("{'likes': ?0}")
    List<User> findByLikesContaining(String like);
    
    /**
     * 根据不喜欢的东西查找用户
     * @param dislike 不喜欢的东西
     * @return 用户列表
     */
    @Query("{'dislikes': ?0}")
    List<User> findByDislikesContaining(String dislike);
    
    /**
     * 查找用户数量统计
     * @return 用户总数
     */
    long count();
    
    /**
     * 根据性别统计用户数量
     * @param gender 性别
     * @return 用户数量
     */
    long countByGender(String gender);
    
    /**
     * 根据MBTI统计用户数量
     * @param mbti MBTI类型
     * @return 用户数量
     */
    long countByMbti(String mbti);
    
    /**
     * 根据血型统计用户数量
     * @param bloodType 血型
     * @return 用户数量
     */
    long countByBloodType(String bloodType);
    
    /**
     * 根据是否首次登录统计用户数量
     * @param isFirstLogin 是否首次登录
     * @return 用户数量
     */
    long countByIsFirstLogin(Boolean isFirstLogin);
    
    /**
     * 查找今日注册的用户
     * @return 用户列表
     */
    @Query("{'createdAt': {$gte: ?0}}")
    List<User> findTodayRegisteredUsers(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找指定年龄的用户
     * @param age 年龄
     * @return 用户列表
     */
    List<User> findByAge(Integer age);
    
    /**
     * 查找年龄大于等于指定值的用户
     * @param age 年龄
     * @return 用户列表
     */
    @Query("{'age': {$gte: ?0}}")
    List<User> findByAgeGreaterThanEqual(Integer age);
    
    /**
     * 查找年龄小于等于指定值的用户
     * @param age 年龄
     * @return 用户列表
     */
    @Query("{'age': {$lte: ?0}}")
    List<User> findByAgeLessThanEqual(Integer age);
} 