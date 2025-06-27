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
 * - 支持按手机号、昵称等字段查询
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
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);
    
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
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查找用户
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(Integer status);
    
    /**
     * 根据用户类型查找用户
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> findByUserType(Integer userType);
    
    /**
     * 查找在线用户
     * @return 在线用户列表
     */
    @Query("{'status': 1}")
    List<User> findOnlineUsers();
    
    /**
     * 查找最近注册的用户
     * @param limit 限制数量
     * @return 用户列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<User> findRecentUsers(int limit);
    
    /**
     * 查找活跃用户（按最后登录时间排序）
     * @param limit 限制数量
     * @return 用户列表
     */
    @Query(value = "{}", sort = "{'lastLoginTime': -1}")
    List<User> findActiveUsers(int limit);
    
    /**
     * 根据标签查找用户
     * @param tag 标签
     * @return 用户列表
     */
    @Query("{'tags': ?0}")
    List<User> findByTag(String tag);
    
    /**
     * 根据兴趣领域查找用户
     * @param interest 兴趣领域
     * @return 用户列表
     */
    @Query("{'interests': ?0}")
    List<User> findByInterest(String interest);
    
    /**
     * 查找用户数量统计
     * @return 用户总数
     */
    long count();
    
    /**
     * 根据状态统计用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(Integer status);
    
    /**
     * 根据用户类型统计用户数量
     * @param userType 用户类型
     * @return 用户数量
     */
    long countByUserType(Integer userType);
    
    /**
     * 查找今日注册的用户
     * @return 用户列表
     */
    @Query("{'createTime': {$gte: ?0}}")
    List<User> findTodayRegisteredUsers(java.time.LocalDateTime startOfDay);
    
    /**
     * 查找今日登录的用户
     * @return 用户列表
     */
    @Query("{'lastLoginTime': {$gte: ?0}}")
    List<User> findTodayLoginUsers(java.time.LocalDateTime startOfDay);
    
    /**
     * 根据昵称模糊查询用户
     * @param nickname 昵称关键词
     * @return 用户列表
     */
    @Query("{'nickname': {$regex: ?0, $options: 'i'}}")
    List<User> findByNicknameContaining(String nickname);
    
    /**
     * 根据个人简介模糊查询用户
     * @param bio 个人简介关键词
     * @return 用户列表
     */
    @Query("{'bio': {$regex: ?0, $options: 'i'}}")
    List<User> findByBioContaining(String bio);
    
    /**
     * 查找指定等级的用户
     * @param level 用户等级
     * @return 用户列表
     */
    List<User> findByLevel(Integer level);
    
    /**
     * 查找等级大于等于指定值的用户
     * @param level 用户等级
     * @return 用户列表
     */
    @Query("{'level': {$gte: ?0}}")
    List<User> findByLevelGreaterThanEqual(Integer level);
    
    /**
     * 查找经验值大于等于指定值的用户
     * @param experience 经验值
     * @return 用户列表
     */
    @Query("{'experience': {$gte: ?0}}")
    List<User> findByExperienceGreaterThanEqual(Integer experience);
    
    /**
     * 查找活跃度大于等于指定值的用户
     * @param activityLevel 活跃度
     * @return 用户列表
     */
    @Query("{'activityLevel': {$gte: ?0}}")
    List<User> findByActivityLevelGreaterThanEqual(Integer activityLevel);
    
    /**
     * 查找友好度大于等于指定值的用户
     * @param friendliness 友好度
     * @return 用户列表
     */
    @Query("{'friendliness': {$gte: ?0}}")
    List<User> findByFriendlinessGreaterThanEqual(Integer friendliness);
    
    /**
     * 查找创造力大于等于指定值的用户
     * @param creativity 创造力
     * @return 用户列表
     */
    @Query("{'creativity': {$gte: ?0}}")
    List<User> findByCreativityGreaterThanEqual(Integer creativity);
    
    /**
     * 查找幽默感大于等于指定值的用户
     * @param humor 幽默感
     * @return 用户列表
     */
    @Query("{'humor': {$gte: ?0}}")
    List<User> findByHumorGreaterThanEqual(Integer humor);
    
    /**
     * 查找知识面大于等于指定值的用户
     * @param knowledge 知识面
     * @return 用户列表
     */
    @Query("{'knowledge': {$gte: ?0}}")
    List<User> findByKnowledgeGreaterThanEqual(Integer knowledge);
    
    /**
     * 查找被禁用的用户
     * @return 用户列表
     */
    @Query("{'enabled': false}")
    List<User> findDisabledUsers();
    
    /**
     * 查找启用的用户
     * @return 用户列表
     */
    @Query("{'enabled': true}")
    List<User> findEnabledUsers();
    
    /**
     * 查找VIP用户
     * @return 用户列表
     */
    @Query("{'isVip': true}")
    List<User> findVipUsers();
    
    /**
     * 查找非VIP用户
     * @return 用户列表
     */
    @Query("{'isVip': false}")
    List<User> findNonVipUsers();
    
    /**
     * 查找已验证邮箱的用户
     * @return 用户列表
     */
    @Query("{'emailVerified': true}")
    List<User> findEmailVerifiedUsers();
    
    /**
     * 查找未验证邮箱的用户
     * @return 用户列表
     */
    @Query("{'emailVerified': false}")
    List<User> findEmailUnverifiedUsers();
    
    /**
     * 查找已验证手机的用户
     * @return 用户列表
     */
    @Query("{'phoneVerified': true}")
    List<User> findPhoneVerifiedUsers();
    
    /**
     * 查找未验证手机的用户
     * @return 用户列表
     */
    @Query("{'phoneVerified': false}")
    List<User> findPhoneUnverifiedUsers();
} 