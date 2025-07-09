package com.myeden.repository;

import com.myeden.entity.SearchContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * SearchContentRepository
 * 支持search_content集合的基本增删查改及多维检索
 */
public interface SearchContentRepository extends MongoRepository<SearchContent, String> {

    /**
     * 根据关键词模糊查询所有包含该关键词的搜索结果（title或summary）
     */
    @Query("{'results': {$elemMatch: {$or: [{'title': {$regex: ?0, $options: 'i'}}, {'summary': {$regex: ?0, $options: 'i'}}]}}}")
    List<SearchContent> findByKeywordInResults(String keyword);

    /**
     * 根据内容类型查询
     */
    List<SearchContent> findBySourceType(String sourceType);

    /**
     * 查询某一时间段内的内容
     */
    List<SearchContent> findByCreatedAtBetween(Date start, Date end);

    /**
     * 查询某个来源的所有结果
     */
    @Query("{'results.source': ?0}")
    List<SearchContent> findByResultSource(String source);

    /**
     * 复合查询：按类型、关键词、时间段联合过滤
     */
    @Query("{'sourceType': ?0, 'results': {$elemMatch: {$or: [{'title': {$regex: ?1, $options: 'i'}}, {'summary': {$regex: ?1, $options: 'i'}}]}}, 'createdAt': {$gte: ?2, $lte: ?3}}")
    List<SearchContent> complexSearch(String sourceType, String keyword, Date start, Date end);

    /**
     * 根据原始搜索关键词查找历史内容
     */
    SearchContent findByQuery(String query);
} 