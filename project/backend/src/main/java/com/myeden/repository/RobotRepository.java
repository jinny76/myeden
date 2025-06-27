package com.myeden.repository;

import com.myeden.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器人数据访问层
 * 
 * 功能说明：
 * - 提供机器人相关的数据库操作
 * - 支持按状态、等级、兴趣等字段查询
 * - 提供机器人统计和行为查询
 * - 支持机器人数据分页和排序
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface RobotRepository extends MongoRepository<Robot, String> {
    
    /**
     * 根据名称查找机器人
     * @param name 机器人名称
     * @return 机器人信息
     */
    Robot findByName(String name);
    
    /**
     * 根据昵称查找机器人
     * @param nickname 机器人昵称
     * @return 机器人信息
     */
    Robot findByNickname(String nickname);
    
    /**
     * 检查名称是否存在
     * @param name 机器人名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查昵称是否存在
     * @param nickname 机器人昵称
     * @return 是否存在
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 根据状态查找机器人
     * @param status 机器人状态
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    Page<Robot> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 查找在线机器人
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'status': 1}")
    Page<Robot> findOnlineRobots(Pageable pageable);
    
    /**
     * 查找启用的机器人
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'enabled': true}")
    Page<Robot> findEnabledRobots(Pageable pageable);
    
    /**
     * 查找禁用的机器人
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'enabled': false}")
    Page<Robot> findDisabledRobots(Pageable pageable);
    
    /**
     * 根据等级查找机器人
     * @param level 机器人等级
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    Page<Robot> findByLevel(Integer level, Pageable pageable);
    
    /**
     * 查找高等级机器人
     * @param minLevel 最小等级
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'level': {$gte: ?0}}")
    Page<Robot> findByLevelGreaterThanEqual(Integer minLevel, Pageable pageable);
    
    /**
     * 根据活跃度查找机器人
     * @param minActivityLevel 最小活跃度
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'activityLevel': {$gte: ?0}}")
    Page<Robot> findByActivityLevelGreaterThanEqual(Integer minActivityLevel, Pageable pageable);
    
    /**
     * 根据友好度查找机器人
     * @param minFriendliness 最小友好度
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'friendliness': {$gte: ?0}}")
    Page<Robot> findByFriendlinessGreaterThanEqual(Integer minFriendliness, Pageable pageable);
    
    /**
     * 根据创造力查找机器人
     * @param minCreativity 最小创造力
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'creativity': {$gte: ?0}}")
    Page<Robot> findByCreativityGreaterThanEqual(Integer minCreativity, Pageable pageable);
    
    /**
     * 根据幽默感查找机器人
     * @param minHumor 最小幽默感
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'humor': {$gte: ?0}}")
    Page<Robot> findByHumorGreaterThanEqual(Integer minHumor, Pageable pageable);
    
    /**
     * 根据知识面查找机器人
     * @param minKnowledge 最小知识面
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'knowledge': {$gte: ?0}}")
    Page<Robot> findByKnowledgeGreaterThanEqual(Integer minKnowledge, Pageable pageable);
    
    /**
     * 根据标签查找机器人
     * @param tag 标签
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'tags': ?0}")
    Page<Robot> findByTag(String tag, Pageable pageable);
    
    /**
     * 根据多个标签查找机器人
     * @param tags 标签列表
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'tags': {$in: ?0}}")
    Page<Robot> findByTags(List<String> tags, Pageable pageable);
    
    /**
     * 根据兴趣领域查找机器人
     * @param interest 兴趣领域
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'interests': ?0}")
    Page<Robot> findByInterest(String interest, Pageable pageable);
    
    /**
     * 根据技能查找机器人
     * @param skill 技能
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'skills': ?0}")
    Page<Robot> findBySkill(String skill, Pageable pageable);
    
    /**
     * 查找活跃机器人（按最后活跃时间排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'lastActiveTime': -1}")
    Page<Robot> findActiveRobots(Pageable pageable);
    
    /**
     * 查找高等级机器人（按等级排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'level': -1}")
    Page<Robot> findHighLevelRobots(Pageable pageable);
    
    /**
     * 查找高活跃度机器人（按活跃度排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'activityLevel': -1}")
    Page<Robot> findHighActivityRobots(Pageable pageable);
    
    /**
     * 查找高友好度机器人（按友好度排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'friendliness': -1}")
    Page<Robot> findHighFriendlinessRobots(Pageable pageable);
    
    /**
     * 查找高创造力机器人（按创造力排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'creativity': -1}")
    Page<Robot> findHighCreativityRobots(Pageable pageable);
    
    /**
     * 查找高幽默感机器人（按幽默感排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'humor': -1}")
    Page<Robot> findHighHumorRobots(Pageable pageable);
    
    /**
     * 查找高知识面机器人（按知识面排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'knowledge': -1}")
    Page<Robot> findHighKnowledgeRobots(Pageable pageable);
    
    /**
     * 查找高优先级机器人（按优先级排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'priority': -1}")
    Page<Robot> findHighPriorityRobots(Pageable pageable);
    
    /**
     * 查找高互动次数机器人（按总互动次数排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'totalInteractions': -1}")
    Page<Robot> findHighInteractionRobots(Pageable pageable);
    
    /**
     * 查找高发布动态数机器人（按总发布动态数排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'totalPosts': -1}")
    Page<Robot> findHighPostRobots(Pageable pageable);
    
    /**
     * 查找高评论数机器人（按总评论数排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'totalComments': -1}")
    Page<Robot> findHighCommentRobots(Pageable pageable);
    
    /**
     * 查找高回复数机器人（按总回复数排序）
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query(value = "{}", sort = "{'totalReplies': -1}")
    Page<Robot> findHighReplyRobots(Pageable pageable);
    
    /**
     * 根据名称或昵称模糊查询机器人
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'nickname': {$regex: ?0, $options: 'i'}}]}")
    Page<Robot> findByNameOrNicknameContaining(String keyword, Pageable pageable);
    
    /**
     * 根据描述模糊查询机器人
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    Page<Robot> findByDescriptionContaining(String keyword, Pageable pageable);
    
    /**
     * 根据性格设定模糊查询机器人
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'personality': {$regex: ?0, $options: 'i'}}")
    Page<Robot> findByPersonalityContaining(String keyword, Pageable pageable);
    
    /**
     * 根据背景故事模糊查询机器人
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'background': {$regex: ?0, $options: 'i'}}")
    Page<Robot> findByBackgroundContaining(String keyword, Pageable pageable);
    
    /**
     * 查找指定时间范围内创建的机器人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'createTime': {$gte: ?0, $lte: ?1}}")
    Page<Robot> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 查找今日创建的机器人
     * @param startOfDay 今日开始时间
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'createTime': {$gte: ?0}}")
    Page<Robot> findTodayCreatedRobots(LocalDateTime startOfDay, Pageable pageable);
    
    /**
     * 查找今日活跃的机器人
     * @param startOfDay 今日开始时间
     * @param pageable 分页参数
     * @return 机器人分页结果
     */
    @Query("{'lastActiveTime': {$gte: ?0}}")
    Page<Robot> findTodayActiveRobots(LocalDateTime startOfDay, Pageable pageable);
    
    /**
     * 统计机器人总数
     * @return 机器人总数
     */
    long count();
    
    /**
     * 统计在线机器人数量
     * @return 机器人数量
     */
    @Query(value = "{'status': 1}", count = true)
    long countOnlineRobots();
    
    /**
     * 统计启用的机器人数量
     * @return 机器人数量
     */
    @Query(value = "{'enabled': true}", count = true)
    long countEnabledRobots();
    
    /**
     * 统计禁用的机器人数量
     * @return 机器人数量
     */
    @Query(value = "{'enabled': false}", count = true)
    long countDisabledRobots();
    
    /**
     * 统计指定等级的机器人数量
     * @param level 机器人等级
     * @return 机器人数量
     */
    long countByLevel(Integer level);
    
    /**
     * 统计高等级机器人数量
     * @param minLevel 最小等级
     * @return 机器人数量
     */
    @Query(value = "{'level': {$gte: ?0}}", count = true)
    long countByLevelGreaterThanEqual(Integer minLevel);
    
    /**
     * 统计今日创建的机器人数量
     * @param startOfDay 今日开始时间
     * @return 机器人数量
     */
    @Query(value = "{'createTime': {$gte: ?0}}", count = true)
    long countTodayCreatedRobots(LocalDateTime startOfDay);
    
    /**
     * 统计今日活跃的机器人数量
     * @param startOfDay 今日开始时间
     * @return 机器人数量
     */
    @Query(value = "{'lastActiveTime': {$gte: ?0}}", count = true)
    long countTodayActiveRobots(LocalDateTime startOfDay);
    
    /**
     * 查找所有机器人（不分页）
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'createTime': -1}")
    List<Robot> findAllOrderByCreateTimeDesc();
    
    /**
     * 查找所有启用的机器人（不分页）
     * @return 机器人列表
     */
    @Query(value = "{'enabled': true}", sort = "{'priority': -1}")
    List<Robot> findAllEnabledRobotsOrderByPriority();
    
    /**
     * 查找所有在线机器人（不分页）
     * @return 机器人列表
     */
    @Query(value = "{'status': 1}", sort = "{'lastActiveTime': -1}")
    List<Robot> findAllOnlineRobotsOrderByLastActiveTime();
    
    /**
     * 查找高等级机器人（不分页）
     * @param limit 限制数量
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'level': -1}")
    List<Robot> findHighLevelRobots(int limit);
    
    /**
     * 查找高活跃度机器人（不分页）
     * @param limit 限制数量
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'activityLevel': -1}")
    List<Robot> findHighActivityRobots(int limit);
    
    /**
     * 查找高优先级机器人（不分页）
     * @param limit 限制数量
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'priority': -1}")
    List<Robot> findHighPriorityRobots(int limit);
    
    /**
     * 查找高互动次数机器人（不分页）
     * @param limit 限制数量
     * @return 机器人列表
     */
    @Query(value = "{}", sort = "{'totalInteractions': -1}")
    List<Robot> findHighInteractionRobots(int limit);
} 