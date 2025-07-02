package com.myeden.controller;

import com.myeden.entity.RobotDailyPlan;
import com.myeden.service.RobotDailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 机器人每日计划相关接口
 */
@RestController
@RequestMapping("/api/v1/robot/plan")
public class RobotDailyPlanController {
    @Autowired
    private RobotDailyPlanService planService;

    /**
     * 查询指定机器人某天的计划
     * @param robotId 机器人ID
     * @param planDate 计划日期（yyyy-MM-dd）
     * @return 计划详情
     */
    @GetMapping
    public EventResponse getPlan(@RequestParam String robotId,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        if (robotId == null || robotId.isEmpty()) {
            return EventResponse.error(400, "robotId不能为空");
        }
        Optional<RobotDailyPlan> planOpt = planService.getPlan(robotId, planDate);
        return planOpt.map(plan -> EventResponse.success(plan, "查询成功"))
                .orElseGet(() -> EventResponse.error(404, "未找到计划"));
    }

    /**
     * 生成指定机器人某天的AI计划
     * @param robotId 机器人ID
     * @param planDate 计划日期（yyyy-MM-dd）
     * @return 生成后的计划
     */
    @PostMapping("/ai")
    public EventResponse generatePlanByAI(@RequestParam String robotId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        try {
            RobotDailyPlan plan = planService.generatePlanByAI(robotId, planDate);
            return EventResponse.success(plan, "AI计划生成任务已提交");
        } catch (UnsupportedOperationException e) {
            return EventResponse.error(501, "AI生成计划功能待实现");
        } catch (Exception e) {
            return EventResponse.error(500, "生成AI计划失败: " + e.getMessage());
        }
    }

    /**
     * 新增每日计划
     * @param plan 计划对象（JSON）
     * @return 保存后的计划
     */
    @PostMapping
    public EventResponse createPlan(@RequestBody RobotDailyPlan plan) {
        try {
            RobotDailyPlan saved = planService.savePlan(plan);
            return EventResponse.success(saved, "新增计划成功");
        } catch (Exception e) {
            return EventResponse.error(500, "新增计划失败: " + e.getMessage());
        }
    }

    /**
     * 更新每日计划
     * @param id 计划ID
     * @param plan 新的计划内容（JSON）
     * @return 更新后的计划
     */
    @PutMapping("/{id}")
    public EventResponse updatePlan(@PathVariable String id, @RequestBody RobotDailyPlan plan) {
        try {
            RobotDailyPlan updated = planService.updatePlan(id, plan);
            return EventResponse.success(updated, "更新计划成功");
        } catch (IllegalArgumentException e) {
            return EventResponse.error(404, "计划不存在");
        } catch (Exception e) {
            return EventResponse.error(500, "更新计划失败: " + e.getMessage());
        }
    }

    /**
     * 软删除每日计划
     * @param id 计划ID
     */
    @DeleteMapping("/{id}")
    public EventResponse deletePlan(@PathVariable String id) {
        try {
            planService.deletePlan(id);
            return EventResponse.success(null, "删除计划成功");
        } catch (IllegalArgumentException e) {
            return EventResponse.error(404, "计划不存在");
        } catch (Exception e) {
            return EventResponse.error(500, "删除计划失败: " + e.getMessage());
        }
    }

    /**
     * 分页/条件查询每日计划
     * @param robotId 机器人ID（可选）
     * @param startDate 起始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页数量（默认20）
     * @return 计划列表
     */
    @GetMapping("/list")
    public EventResponse listPlans(@RequestParam(required = false) String robotId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        try {
            return EventResponse.success(planService.listPlans(robotId, startDate, endDate, page, size), "查询成功");
        } catch (Exception e) {
            return EventResponse.error(500, "查询计划失败: " + e.getMessage());
        }
    }

    /**
     * 手动批量生成所有机器人的今日计划（测试/运维用）
     */
    @PostMapping("/batch/ai")
    public EventResponse batchGenerateTodayPlans() {
        try {
            LocalDate today = LocalDate.now();
            planService.batchGenerateAllRobotsPlanByAI(today);
            return EventResponse.success(null, "批量生成任务已提交，稍后自动完成。");
        } catch (Exception e) {
            return EventResponse.error(500, "批量生成失败: " + e.getMessage());
        }
    }
} 