package com.myeden.repository;

import com.myeden.entity.UserSetting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户设置数据访问层
 * 
 * 功能说明：
 * - 提供用户设置相关的数据库操作
 * - 支持按用户ID查询设置
 * - 提供设置的存在性检查
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface UserSettingRepository extends MongoRepository<UserSetting, String> {
    
    /**
     * 根据用户ID查找用户设置
     * @param userId 用户ID
     * @return 用户设置（可能为空）
     */
    Optional<UserSetting> findByUserId(String userId);
    
    /**
     * 检查用户是否存在设置记录
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByUserId(String userId);
    
    /**
     * 根据用户ID删除设置
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
} 