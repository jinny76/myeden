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
} 