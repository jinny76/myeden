package com.myeden.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 数据库索引配置类
 * 负责MongoDB索引的创建和优化
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
public class IndexConfig {

    private static final Logger logger = LoggerFactory.getLogger(IndexConfig.class);

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Autowired
    private MongoClient mongoClient;

    /**
     * 初始化数据库索引
     * 在应用启动时自动创建必要的索引以优化查询性能
     */
    @PostConstruct
    public void initIndexes() {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
            
            // 用户集合索引
            createUserIndexes(mongoDatabase);
            
            // 动态集合索引
            createPostIndexes(mongoDatabase);
            
            // 评论集合索引
            createCommentIndexes(mongoDatabase);
            
            // 机器人集合索引
            createRobotIndexes(mongoDatabase);
            
            // 点赞记录索引
            createLikeIndexes(mongoDatabase);
            
            logger.info("数据库索引初始化完成");
        } catch (Exception e) {
            logger.error("数据库索引初始化失败", e);
        }
    }

    /**
     * 安全创建索引
     * 检查索引是否已存在，避免重复创建
     */
    private void createIndexSafely(MongoDatabase database, String collectionName, 
                                  org.bson.Document indexDocument, IndexOptions options) {
        try {
            var collection = database.getCollection(collectionName);
            
            // 检查索引是否已存在
            boolean indexExists = false;
            for (var index : collection.listIndexes()) {
                if (index.containsKey("name") && index.getString("name").equals(options.getName())) {
                    indexExists = true;
                    break;
                }
            }
            
            if (!indexExists) {
                collection.createIndex(indexDocument, options);
                logger.info("索引创建成功: {} - {}", collectionName, options.getName());
            } else {
                logger.info("索引已存在，跳过创建: {} - {}", collectionName, options.getName());
            }
        } catch (Exception e) {
            logger.warn("创建索引失败: {} - {}, 错误: {}", collectionName, options.getName(), e.getMessage());
        }
    }

    /**
     * 创建用户集合索引
     * 优化用户查询、登录、注册等操作
     */
    private void createUserIndexes(MongoDatabase database) {
        var collection = database.getCollection("users");
        
        // 用户ID唯一索引
        createIndexSafely(database, "users", 
            new org.bson.Document("userId", 1).append("createdAt", -1),
            new IndexOptions().unique(true).name("idx_user_id_unique")
        );
        
        // 手机号唯一索引
        createIndexSafely(database, "users", 
            new org.bson.Document("phone", 1),
            new IndexOptions().unique(true).name("idx_phone_unique")
        );
        
        // 创建时间索引（用于排序）
        createIndexSafely(database, "users", 
            new org.bson.Document("createdAt", -1),
            new IndexOptions().name("idx_created_at_desc")
        );
        
        // 昵称索引（用于搜索）
        createIndexSafely(database, "users", 
            new org.bson.Document("$**", "text"),
            new IndexOptions().name("idx_nickname_text")
        );
        
        logger.info("用户集合索引创建完成");
    }

    /**
     * 创建动态集合索引
     * 优化动态查询、分页、排序等操作
     */
    private void createPostIndexes(MongoDatabase database) {
        var collection = database.getCollection("posts");
        
        // 动态ID唯一索引
        createIndexSafely(database, "posts", 
            new org.bson.Document("postId", 1),
            new IndexOptions().unique(true).name("idx_post_id_unique")
        );
        
        // 作者ID索引
        createIndexSafely(database, "posts", 
            new org.bson.Document("authorId", 1),
            new IndexOptions().name("idx_author_id")
        );
        
        // 创建时间索引（用于排序和分页）
        createIndexSafely(database, "posts", 
            new org.bson.Document("createdAt", -1),
            new IndexOptions().name("idx_post_created_at_desc")
        );
        
        // 复合索引：作者ID + 创建时间（用于用户动态查询）
        createIndexSafely(database, "posts", 
            new org.bson.Document("authorId", 1).append("createdAt", -1),
            new IndexOptions().name("idx_author_created_compound")
        );
        
        // 内容文本索引（用于搜索）
        createIndexSafely(database, "posts", 
            new org.bson.Document("$**", "text"),
            new IndexOptions().name("idx_content_text")
        );
        
        // 软删除索引
        createIndexSafely(database, "posts", 
            new org.bson.Document("deleted", 1),
            new IndexOptions().name("idx_deleted")
        );
        
        logger.info("动态集合索引创建完成");
    }

    /**
     * 创建评论集合索引
     * 优化评论查询、回复、分页等操作
     */
    private void createCommentIndexes(MongoDatabase database) {
        var collection = database.getCollection("comments");
        
        // 评论ID唯一索引
        createIndexSafely(database, "comments", 
            new org.bson.Document("commentId", 1),
            new IndexOptions().unique(true).name("idx_comment_id_unique")
        );
        
        // 动态ID索引
        createIndexSafely(database, "comments", 
            new org.bson.Document("postId", 1),
            new IndexOptions().name("idx_comment_post_id")
        );
        
        // 作者ID索引
        createIndexSafely(database, "comments", 
            new org.bson.Document("authorId", 1),
            new IndexOptions().name("idx_comment_author_id")
        );
        
        // 父评论ID索引（用于回复查询）
        createIndexSafely(database, "comments", 
            new org.bson.Document("parentId", 1),
            new IndexOptions().name("idx_comment_parent_id")
        );
        
        // 创建时间索引
        createIndexSafely(database, "comments", 
            new org.bson.Document("createdAt", -1),
            new IndexOptions().name("idx_comment_created_at_desc")
        );
        
        // 复合索引：动态ID + 创建时间（用于动态评论查询）
        createIndexSafely(database, "comments", 
            new org.bson.Document("postId", 1).append("createdAt", -1),
            new IndexOptions().name("idx_comment_post_created_compound")
        );
        
        // 软删除索引
        createIndexSafely(database, "comments", 
            new org.bson.Document("deleted", 1),
            new IndexOptions().name("idx_comment_deleted")
        );
        
        logger.info("评论集合索引创建完成");
    }

    /**
     * 创建机器人集合索引
     * 优化机器人查询、状态管理等操作
     */
    private void createRobotIndexes(MongoDatabase database) {
        var collection = database.getCollection("robots");
        
        // 机器人ID唯一索引
        createIndexSafely(database, "robots", 
            new org.bson.Document("robotId", 1),
            new IndexOptions().unique(true).name("idx_robot_id_unique")
        );
        
        // 活跃状态索引
        createIndexSafely(database, "robots", 
            new org.bson.Document("isActive", 1),
            new IndexOptions().name("idx_robot_active")
        );
        
        // 创建时间索引
        createIndexSafely(database, "robots", 
            new org.bson.Document("createdAt", -1),
            new IndexOptions().name("idx_robot_created_at_desc")
        );
        
        // 名称文本索引（用于搜索）
        createIndexSafely(database, "robots", 
            new org.bson.Document("$**", "text"),
            new IndexOptions().name("idx_robot_name_text")
        );
        
        logger.info("机器人集合索引创建完成");
    }

    /**
     * 创建点赞记录索引
     * 优化点赞查询、防重复点赞等操作
     */
    private void createLikeIndexes(MongoDatabase database) {
        // 动态点赞索引
        var postLikeCollection = database.getCollection("post_likes");
        createIndexSafely(database, "post_likes", 
            new org.bson.Document("postId", 1).append("userId", 1),
            new IndexOptions().unique(true).name("idx_post_like_unique")
        );
        createIndexSafely(database, "post_likes", 
            new org.bson.Document("postId", 1),
            new IndexOptions().name("idx_post_like_post_id")
        );
        createIndexSafely(database, "post_likes", 
            new org.bson.Document("userId", 1),
            new IndexOptions().name("idx_post_like_user_id")
        );
        
        // 评论点赞索引
        var commentLikeCollection = database.getCollection("comment_likes");
        createIndexSafely(database, "comment_likes", 
            new org.bson.Document("commentId", 1).append("userId", 1),
            new IndexOptions().unique(true).name("idx_comment_like_unique")
        );
        createIndexSafely(database, "comment_likes", 
            new org.bson.Document("commentId", 1),
            new IndexOptions().name("idx_comment_like_comment_id")
        );
        createIndexSafely(database, "comment_likes", 
            new org.bson.Document("userId", 1),
            new IndexOptions().name("idx_comment_like_user_id")
        );
        
        logger.info("点赞记录索引创建完成");
    }
} 