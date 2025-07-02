package com.myeden.repository;

import com.myeden.entity.RobotDailyPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 机器人每日计划Repository
 * 支持按robotId、planDate查询
 */
@Repository
public interface RobotDailyPlanRepository extends MongoRepository<RobotDailyPlan, String> {
    Optional<RobotDailyPlan> findByRobotIdAndPlanDate(String robotId, LocalDate planDate);
    List<RobotDailyPlan> findByRobotId(String robotId);
    List<RobotDailyPlan> findByPlanDate(LocalDate planDate);
} 