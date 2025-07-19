package com.myeden.repository;

import com.myeden.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);    

    @Query(
      value = "{ $or: [ { $and: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }, { $and: [ { 'senderId': ?1 }, { 'receiverId': ?0 } ] } ] }",
      sort = "{ 'createdAt': -1 }"
    )
    List<ChatMessage> findHistoryWithRobotDesc(String userId, String robotId, Pageable pageable);

    @Query(
      value = "{ $and: [ { $or: [ { $and: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }, { $and: [ { 'senderId': ?1 }, { 'receiverId': ?0 } ] } ] }, { 'createdAt': { $lt: ?2 } } ] }",
      sort = "{ 'createdAt': -1 }"
    )
    List<ChatMessage> findHistoryWithRobotBeforeDesc(String userId, String robotId, java.time.LocalDateTime before, Pageable pageable);
    
    /**
     * 获取用户的所有不同对话ID
     * 
     * @param userId 用户ID
     * @return 对话ID列表
     */
    @Query(value = "{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }", fields = "{ 'conversationId': 1 }")
    List<ChatMessage> findConversationIdsByUserId(String userId);
    
    /**
     * 获取用户的所有不同对话ID（默认方法）
     * 
     * @param userId 用户ID
     * @return 对话ID列表
     */
    default List<String> findDistinctConversationIdsByUserId(String userId) {
        return findConversationIdsByUserId(userId).stream()
            .map(ChatMessage::getConversationId)
            .filter(id -> id != null && !id.trim().isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 根据对话ID获取消息，按创建时间升序排列
     * 
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    
    /**
     * 获取用户最新的N条消息
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 消息列表
     */
    @Query(
      value = "{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }",
      sort = "{ 'createdAt': -1 }"
    )
    List<ChatMessage> findTopByUserIdOrderByCreatedAtDesc(String userId, int limit);
    
    /**
     * 获取用户在指定日期范围内的对话ID
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对话消息列表
     */
    @Query(
      value = "{ $and: [ { $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }, { 'createdAt': { $gte: ?1, $lte: ?2 } } ] }",
      fields = "{ 'conversationId': 1 }"
    )
    List<ChatMessage> findConversationIdsByUserIdAndDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * 获取用户在指定日期范围内的所有不同对话ID（默认方法）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对话ID列表
     */
    default List<String> findDistinctConversationIdsByUserIdAndDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return findConversationIdsByUserIdAndDateRange(userId, startDate, endDate).stream()
            .map(ChatMessage::getConversationId)
            .filter(id -> id != null && !id.trim().isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询指定用户在指定时间区间内的消息，按创建时间降序排列
     * @param userId 用户ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 消息列表
     */
    @Query(value = "{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ], 'createdAt': { $gte: ?1, $lte: ?2 } }", sort = "{ 'createdAt': -1 }")
    List<ChatMessage> findByUserIdAndCreatedAtBetween(String userId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * 根据expertThemeId过滤用户与机器人的历史消息，按创建时间降序排列
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param expertThemeId 专家主题ID（null表示随便聊聊模式）
     * @param pageable 分页参数
     * @return 过滤后的消息列表
     */
    @Query(
      value = "{ $and: [ { $or: [ { $and: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }, { $and: [ { 'senderId': ?1 }, { 'receiverId': ?0 } ] } ] }, { 'expertThemeId': ?2 } ] }",
      sort = "{ 'createdAt': -1 }"
    )
    List<ChatMessage> findHistoryWithRobotByExpertThemeDesc(String userId, String robotId, String expertThemeId, Pageable pageable);

    /**
     * 根据expertThemeId过滤用户与机器人在指定时间之前的历史消息，按创建时间降序排列
     * @param userId 用户ID
     * @param robotId 机器人ID
     * @param expertThemeId 专家主题ID（null表示随便聊聊模式）
     * @param before 指定时间
     * @param pageable 分页参数
     * @return 过滤后的消息列表
     */
    @Query(
      value = "{ $and: [ { $or: [ { $and: [ { 'senderId': ?0 }, { 'receiverId': ?1 } ] }, { $and: [ { 'senderId': ?1 }, { 'receiverId': ?0 } ] } ] }, { 'expertThemeId': ?2 }, { 'createdAt': { $lt: ?3 } } ] }",
      sort = "{ 'createdAt': -1 }"
    )
    List<ChatMessage> findHistoryWithRobotByExpertThemeBeforeDesc(String userId, String robotId, String expertThemeId, java.time.LocalDateTime before, Pageable pageable);
} 