package com.myeden.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 身份验证注解
 * 
 * 功能说明：
 * - 标记需要JWT身份验证的控制器方法
 * - 确保用户只能操作自己的资源
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuthentication {
    
    /**
     * 是否要求用户只能操作自己的资源
     * 默认为true
     */
    boolean requireOwnership() default true;
    
    /**
     * 错误消息
     */
    String message() default "需要身份验证";
} 