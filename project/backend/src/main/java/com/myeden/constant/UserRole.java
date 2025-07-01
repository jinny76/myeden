package com.myeden.constant;

/**
 * 用户角色常量类
 * 
 * 功能说明：
 * - 定义系统中可用的用户角色
 * - 提供角色相关的常量和方法
 * - 确保角色定义的一致性
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class UserRole {
    
    /**
     * 普通用户角色
     */
    public static final String USER = "user";
    
    /**
     * 版主角色
     */
    public static final String MODERATOR = "moderator";
    
    /**
     * 管理员角色
     */
    public static final String ADMIN = "admin";
    
    /**
     * 超级管理员角色
     */
    public static final String SUPER_ADMIN = "super_admin";
    
    /**
     * 私有构造函数，防止实例化
     */
    private UserRole() {
        throw new UnsupportedOperationException("常量类不能实例化");
    }
    
    /**
     * 检查角色是否有效
     * 
     * @param role 要检查的角色
     * @return 如果角色有效返回true，否则返回false
     */
    public static boolean isValidRole(String role) {
        return USER.equals(role) || 
               MODERATOR.equals(role) || 
               ADMIN.equals(role) || 
               SUPER_ADMIN.equals(role);
    }
    
    /**
     * 获取角色等级
     * 等级越高，权限越大
     * 
     * @param role 角色
     * @return 角色等级（0-3）
     */
    public static int getRoleLevel(String role) {
        switch (role) {
            case USER:
                return 0;
            case MODERATOR:
                return 1;
            case ADMIN:
                return 2;
            case SUPER_ADMIN:
                return 3;
            default:
                return -1;
        }
    }
    
    /**
     * 检查角色是否有足够权限
     * 
     * @param userRole 用户角色
     * @param requiredRole 所需角色
     * @return 如果用户角色等级大于等于所需角色等级返回true
     */
    public static boolean hasPermission(String userRole, String requiredRole) {
        int userLevel = getRoleLevel(userRole);
        int requiredLevel = getRoleLevel(requiredRole);
        return userLevel >= requiredLevel;
    }
    
    /**
     * 获取所有可用角色
     * 
     * @return 角色数组
     */
    public static String[] getAllRoles() {
        return new String[]{USER, MODERATOR, ADMIN, SUPER_ADMIN};
    }
    
    /**
     * 获取角色显示名称
     * 
     * @param role 角色
     * @return 角色显示名称
     */
    public static String getRoleDisplayName(String role) {
        switch (role) {
            case USER:
                return "普通用户";
            case MODERATOR:
                return "版主";
            case ADMIN:
                return "管理员";
            case SUPER_ADMIN:
                return "超级管理员";
            default:
                return "未知角色";
        }
    }
    
    /**
     * 获取角色描述
     * 
     * @param role 角色
     * @return 角色描述
     */
    public static String getRoleDescription(String role) {
        switch (role) {
            case USER:
                return "普通用户，可以浏览内容、发表评论、上传头像等基本功能";
            case MODERATOR:
                return "版主，可以管理帖子、删除不当内容、处理用户举报";
            case ADMIN:
                return "管理员，可以管理用户、设置版主、查看系统统计";
            case SUPER_ADMIN:
                return "超级管理员，拥有系统最高权限，可以管理所有管理员";
            default:
                return "未知角色";
        }
    }
} 