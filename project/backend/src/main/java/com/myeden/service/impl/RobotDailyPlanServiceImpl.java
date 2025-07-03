package com.myeden.service.impl;

import com.myeden.entity.RobotDailyPlan;
import com.myeden.repository.RobotDailyPlanRepository;
import com.myeden.service.RobotDailyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.task.TaskExecutor;
import com.myeden.entity.Robot;
import com.myeden.repository.RobotRepository;
import com.myeden.service.PromptService;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * 机器人每日计划服务实现
 */
@Service
public class RobotDailyPlanServiceImpl implements RobotDailyPlanService {
    @Autowired
    private RobotDailyPlanRepository planRepository;

    @Autowired
    @Qualifier("aiTaskExecutor")
    private Executor taskExecutor;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private PromptService promptService;

    @Override
    public Optional<RobotDailyPlan> getPlan(String robotId, LocalDate planDate) {
        return planRepository.findByRobotIdAndPlanDate(robotId, planDate);
    }

    @Override
    public RobotDailyPlan.PlanSlot getEventContext(String robotId) {
        // 获取今日计划，查找当前时间所在时间段
        LocalDate today = LocalDate.now();
        Optional<RobotDailyPlan> planOpt = planRepository.findByRobotIdAndPlanDate(robotId, today);
        if (planOpt.isPresent()) {
            RobotDailyPlan plan = planOpt.get();
            String now = java.time.LocalTime.now().toString().substring(0,5); // HH:mm
            for (RobotDailyPlan.PlanSlot slot : plan.getSlots()) {
                if (slot.getStart().compareTo(now) <= 0 && slot.getEnd().compareTo(now) > 0) {
                    return slot;
                }
            }
        }
        return null;
    }

    @Override
    public RobotDailyPlan generatePlanByAI(String robotId, LocalDate planDate) {
        // 1. 检查当天是否已有计划
        Optional<RobotDailyPlan> existing = planRepository.findByRobotIdAndPlanDate(robotId, planDate);
        if (existing.isPresent()) {
            return existing.get();
        }
        // 2. 获取机器人对象
        Optional<Robot> robotOpt = robotRepository.findById(robotId);
        if (!robotOpt.isPresent()) {
            throw new IllegalArgumentException("机器人不存在: " + robotId);
        }
        Robot robot = robotOpt.get();
        // 3. 先插入PENDING计划
        RobotDailyPlan plan = new RobotDailyPlan();
        plan.setRobotId(robotId);
        plan.setRobotName(robot.getName());
        plan.setPlanDate(planDate);
        plan.setDiary("生成中...");
        plan.setStatus("PENDING");
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setIsDeleted(false);
        planRepository.save(plan);

        // 4. 异步AI生成
        taskExecutor.execute(() -> {
            try {
                RobotDailyPlan aiPlan = promptService.generateDailyPlan(robot, planDate);
                plan.setDiary(aiPlan.getDiary());
                plan.setSlots(aiPlan.getSlots());
                plan.setStatus("SUCCESS");
                plan.setUpdatedAt(LocalDateTime.now());
                planRepository.save(plan);
            } catch (Exception e) {
                plan.setStatus("FAILED");
                plan.setErrorMsg(e.getMessage());
                plan.setUpdatedAt(LocalDateTime.now());
                planRepository.save(plan);
            }
        });

        return plan;
    }

    @Override
    public List<RobotDailyPlan> batchGenerateAllRobotsPlanByAI(LocalDate planDate) {
        List<Robot> robots = robotRepository.findAll();
        robots = robots.stream()
                .filter(r -> r.getIsDeleted() == null || !r.getIsDeleted())
                .collect(java.util.stream.Collectors.toList());
        List<RobotDailyPlan> result = new ArrayList<>();
        Semaphore semaphore = new Semaphore(2); // 最多2个并发

        for (Robot robot : robots) {
            Optional<RobotDailyPlan> existing = planRepository.findByRobotIdAndPlanDate(robot.getRobotId(), planDate);
            if (existing.isPresent()) continue;

            RobotDailyPlan plan = new RobotDailyPlan();
            plan.setRobotId(robot.getRobotId());
            plan.setRobotName(robot.getName());
            plan.setPlanDate(planDate);
            plan.setDiary("生成中...");
            plan.setStatus("PENDING");
            plan.setCreatedAt(LocalDateTime.now());
            plan.setUpdatedAt(LocalDateTime.now());
            plan.setIsDeleted(false);
            planRepository.save(plan);
            result.add(plan);

            // 并发受控的异步AI生成
            taskExecutor.execute(() -> {
                try {
                    semaphore.acquire();
                    try {
                        RobotDailyPlan aiPlan = promptService.generateDailyPlan(robot, planDate);
                        plan.setDiary(aiPlan.getDiary());
                        plan.setSlots(aiPlan.getSlots());
                        plan.setStatus("SUCCESS");
                        plan.setUpdatedAt(LocalDateTime.now());
                        planRepository.save(plan);
                    } catch (Exception e) {
                        plan.setStatus("FAILED");
                        plan.setErrorMsg(e.getMessage());
                        plan.setUpdatedAt(LocalDateTime.now());
                        planRepository.save(plan);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        return result;
    }

    @Override
    public RobotDailyPlan savePlan(RobotDailyPlan plan) {
        // 新增或保存计划，自动设置创建/更新时间
        if (plan.getId() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setIsDeleted(false);
        return planRepository.save(plan);
    }

    @Override
    public RobotDailyPlan updatePlan(String id, RobotDailyPlan plan) {
        // 更新指定ID的计划，保留原有创建时间
        Optional<RobotDailyPlan> existingOpt = planRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("计划不存在: " + id);
        }
        RobotDailyPlan existing = existingOpt.get();
        plan.setId(id);
        plan.setCreatedAt(existing.getCreatedAt());
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setIsDeleted(false);
        return planRepository.save(plan);
    }

    @Override
    public void deletePlan(String id) {
        // 软删除，将isDeleted设为true
        Optional<RobotDailyPlan> existingOpt = planRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("计划不存在: " + id);
        }
        RobotDailyPlan plan = existingOpt.get();
        plan.setIsDeleted(true);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
    }

    @Override
    public List<RobotDailyPlan> listPlans(String robotId, LocalDate startDate, LocalDate endDate, int page, int size) {
        // 过滤已删除和未生成成功，按条件筛选，分页
        List<RobotDailyPlan> all = planRepository.findAll();
        return all.stream()
            .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
            .filter(p -> "SUCCESS".equals(p.getStatus()))
            .filter(p -> robotId == null || robotId.isEmpty() || robotId.equals(p.getRobotId()))
            .filter(p -> (startDate == null || !p.getPlanDate().isBefore(startDate)))
            .filter(p -> (endDate == null || !p.getPlanDate().isAfter(endDate)))
            .sorted((a, b) -> b.getPlanDate().compareTo(a.getPlanDate()))
            .skip((long) page * size)
            .limit(size)
            .toList();
    }
} 