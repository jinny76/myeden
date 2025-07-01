package com.myeden.constant;

/**
 * 作者类型常量类
 * 
 * 功能说明：
 * - 定义系统中可用的作者类型
 * - 提供作者类型相关的常量和方法
 * - 确保作者类型定义的一致性
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class AuthorType {
    
    /**
     * 普通用户
     */
    public static final String USER = "user";
    
    /**
     * 机器人
     */
    public static final String ROBOT = "robot";
    
    /**
     * 机器人ID前缀
     */
    public static final String ROBOT_ID_PREFIX = "robot_";
    
    /**
     * 私有构造函数，防止实例化
     */
    private AuthorType() {
        throw new UnsupportedOperationException("常量类不能实例化");
    }
    
    /**
     * 检查是否为机器人
     * 
     * @param authorId 作者ID
     * @return 如果是机器人返回true，否则返回false
     */
    public static boolean isRobot(String authorId) {
        return authorId != null && authorId.startsWith(ROBOT_ID_PREFIX);
    }
    
    /**
     * 检查是否为普通用户
     * 
     * @param authorId 作者ID
     * @return 如果是普通用户返回true，否则返回false
     */
    public static boolean isUser(String authorId) {
        return authorId != null && !authorId.startsWith(ROBOT_ID_PREFIX);
    }
    
    /**
     * 获取作者类型
     * 
     * @param authorId 作者ID
     * @return 作者类型（user/robot）
     */
    public static String getAuthorType(String authorId) {
        if (isRobot(authorId)) {
            return ROBOT;
        } else {
            return USER;
        }
    }
    
    /**
     * 检查作者类型是否有效
     * 
     * @param authorType 作者类型
     * @return 如果作者类型有效返回true，否则返回false
     */
    public static boolean isValidAuthorType(String authorType) {
        return USER.equals(authorType) || ROBOT.equals(authorType);
    }
    
    /**
     * 获取作者类型显示名称
     * 
     * @param authorType 作者类型
     * @return 作者类型显示名称
     */
    public static String getAuthorTypeDisplayName(String authorType) {
        switch (authorType) {
            case USER:
                return "用户";
            case ROBOT:
                return "机器人";
            default:
                return "未知";
        }
    }
} 