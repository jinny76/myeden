package com.myeden.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import java.util.concurrent.TimeUnit;

/**
 * 数据库配置类
 * 负责MongoDB连接池配置、索引优化、查询性能优化
 * 
 * @author AI助手
 * @version 1.0.0
 * @since 2024-12-19
 */
@Configuration
public class DatabaseConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        // 构建MongoDB连接字符串
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                username, password, host, port, database, authDatabase);

        // 配置MongoDB客户端设置
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                // 连接池配置
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(100)
                        .minSize(5)
                        .maxWaitTime(30000, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(300000, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS))
                // 服务器选择配置
                .applyToClusterSettings(builder -> builder
                        .serverSelectionTimeout(30000, TimeUnit.MILLISECONDS))
                // 连接超时配置
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(30000, TimeUnit.MILLISECONDS))
                // 读写关注配置
                .readConcern(com.mongodb.ReadConcern.MAJORITY)
                .writeConcern(com.mongodb.WriteConcern.MAJORITY)
                .readPreference(com.mongodb.ReadPreference.primary())
                .build();

        logger.info("MongoDB连接配置完成，数据库: {}, 主机: {}:{}", database, host, port);
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());
        
        // 配置MongoTemplate优化设置
        MappingMongoConverter converter = (MappingMongoConverter) mongoTemplate.getConverter();
        converter.setMapKeyDotReplacement("_");
        
        logger.info("MongoTemplate配置完成");
        return mongoTemplate;
    }

    /**
     * 获取数据库统计信息
     */
    public Document getDatabaseStats() {
        try {
            MongoDatabase mongoDatabase = mongoClient().getDatabase(database);
            return mongoDatabase.runCommand(new Document("dbStats", 1));
        } catch (Exception e) {
            logger.error("获取数据库统计信息失败", e);
            return new Document("error", e.getMessage());
        }
    }

    /**
     * 获取集合统计信息
     */
    public Document getCollectionStats(String collectionName) {
        try {
            MongoDatabase mongoDatabase = mongoClient().getDatabase(database);
            return mongoDatabase.runCommand(new Document("collStats", collectionName));
        } catch (Exception e) {
            logger.error("获取集合统计信息失败: {}", collectionName, e);
            return new Document("error", e.getMessage());
        }
    }
} 