package com.myeden.controller;

import com.myeden.dto.CommunicationReportDto;
import com.myeden.entity.CommunicationReport;
import com.myeden.service.CommunicationScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 沟通评估报告Controller
 * 
 * 功能说明：
 * - 提供用户沟通报告查询接口
 * - 提供用户沟通统计查询接口
 * - 支持分页和筛选功能
 * 
 * @author MyEden Team
 * @version 1.0.0
 * @since 2025-07-15
 */
@RestController
@RequestMapping("/api/v1/communication-reports")
public class CommunicationReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommunicationReportController.class);
    
    @Autowired
    private CommunicationScoringService communicationScoringService;
    
    /**
     * 获取用户的沟通报告列表
     * 
     * @return 响应结果
     */
    @GetMapping
    public ResponseEntity<EventResponse> getUserCommunicationReports() {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            List<CommunicationReportDto> reports = communicationScoringService.getUserCommunicationReportsWithDetails(userId);
            
            return ResponseEntity.ok(new EventResponse(200, "获取沟通报告成功", reports));
            
        } catch (Exception e) {
            logger.error("获取沟通报告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取沟通报告失败", null));
        }
    }
    
    /**
     * 获取用户的沟通报告列表（基础版本，不包含扩展信息）
     * 
     * @return 响应结果
     */
    @GetMapping("/basic")
    public ResponseEntity<EventResponse> getUserCommunicationReportsBasic() {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            List<CommunicationReport> reports = communicationScoringService.getUserCommunicationReports(userId);
            
            return ResponseEntity.ok(new EventResponse(200, "获取基础沟通报告成功", reports));
            
        } catch (Exception e) {
            logger.error("获取基础沟通报告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取基础沟通报告失败", null));
        }
    }
    
    /**
     * 获取用户的沟通统计
     * 
     * @return 响应结果
     */
    @GetMapping("/statistics")
    public ResponseEntity<EventResponse> getUserCommunicationStatistics() {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            CommunicationScoringService.CommunicationStatistics statistics = 
                communicationScoringService.getUserCommunicationStatistics(userId);
            
            return ResponseEntity.ok(new EventResponse(200, "获取沟通统计成功", statistics));
            
        } catch (Exception e) {
            logger.error("获取沟通统计失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取沟通统计失败", null));
        }
    }
    
    /**
     * 获取用户的沟通报告列表（带过滤）
     * 
     * @param robotId 机器人ID过滤（可选）
     * @param startDate 开始日期过滤（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期过滤（可选，格式：yyyy-MM-dd）
     * @return 响应结果
     */
    @GetMapping("/filtered")
    public ResponseEntity<EventResponse> getFilteredCommunicationReports(
            @RequestParam(required = false) String robotId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            List<CommunicationReport> reports = communicationScoringService.getFilteredCommunicationReports(
                userId, robotId, startDate, endDate);
            
            return ResponseEntity.ok(new EventResponse(200, "获取过滤沟通报告成功", reports));
            
        } catch (Exception e) {
            logger.error("获取过滤沟通报告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "获取过滤沟通报告失败", null));
        }
    }
    
    /**
     * 手动触发当前用户的沟通评价
     * 
     * @return 响应结果
     */
    @PostMapping("/trigger-evaluation")
    public ResponseEntity<EventResponse> triggerCommunicationEvaluation() {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            
            logger.info("手动触发用户 {} 的沟通评价", userId);
            
            // 触发当前用户的沟通评价
            communicationScoringService.evaluateUserTodayConversations(userId);
            
            return ResponseEntity.ok(new EventResponse(200, "成功进行了评价", null));
            
        } catch (Exception e) {
            logger.error("手动触发沟通评价失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new EventResponse(400, "触发沟通评价失败", null));
        }
    }
}