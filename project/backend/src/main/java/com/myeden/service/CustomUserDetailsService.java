package com.myeden.service;

import com.myeden.entity.User;
import com.myeden.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 * 
 * 功能说明：
 * - 实现Spring Security的UserDetailsService接口
 * - 根据用户ID加载用户详情
 * - 为Spring Security提供用户认证信息
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 根据用户ID查找用户
        var userOpt = userRepository.findByUserId(userId);
        
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        
        User user = userOpt.get();
        
        // 创建UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
} 