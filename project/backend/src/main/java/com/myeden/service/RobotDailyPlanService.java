package com.myeden.service;

import com.myeden.entity.RobotDailyPlan;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * 机器人每日计划服务接口
 * 提供计划查询、AI生成、批量生成、上下文获取等功能
 */
public interface RobotDailyPlanService {
    /**
     * 获取指定机器人某天的计划
     * @param robotId 机器人ID
     * @param planDate 计划日期
     * @return 计划对象（可为空）
     */
    Optional<RobotDailyPlan> getPlan(String robotId, LocalDate planDate);

    /**
     * 获取机器人当前时间的事件上下文（如当前时间段及事件）
     * @param robotId 机器人ID
     * @return 当前事件上下文（可为空）
     */
    RobotDailyPlan.PlanSlot getEventContext(String robotId);

    /**
     * 调用AI生成指定机器人某天的计划（含日记与多条安排）
     * @param robotId 机器人ID
     * @param planDate 计划日期
     * @return 生成后的计划对象
     */
    RobotDailyPlan generatePlanByAI(String robotId, LocalDate planDate);

    /**
     * 为所有机器人批量生成某天的计划（定时任务用）
     * @param planDate 计划日期
     * @return 生成的计划列表
     */
    List<RobotDailyPlan> batchGenerateAllRobotsPlanByAI(LocalDate planDate);

    /**
     * 新增或保存每日计划
     * @param plan 计划对象
     * @return 保存后的计划对象
     */
    RobotDailyPlan savePlan(RobotDailyPlan plan);

    /**
     * 更新指定ID的每日计划
     * @param id 计划ID
     * @param plan 新的计划内容
     * @return 更新后的计划对象
     */
    RobotDailyPlan updatePlan(String id, RobotDailyPlan plan);

    /**
     * 软删除指定ID的每日计划
     * @param id 计划ID
     */
    void deletePlan(String id);

    /**
     * 分页/条件查询每日计划（可按robotId、日期区间筛选）
     * @param robotId 机器人ID（可选）
     * @param startDate 起始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 计划列表
     */
    List<RobotDailyPlan> listPlans(String robotId, LocalDate startDate, LocalDate endDate, int page, int size);
} 