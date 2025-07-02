package com.myeden.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 机器人每日计划主对象
 * 包含一天内多个时间段，每段可有多个事件和心情，支持软删除。
 */
@Document(collection = "robot_daily_plans")
public class RobotDailyPlan {
    @Id
    private String id;
    /** 关联机器人ID */
    private String robotId;
    /** 日记内容 */
    private String diary;
    /** 计划日期 */
    private LocalDate planDate;
    /** 一天内的时间段安排 */
    private List<PlanSlot> slots = new ArrayList<>();
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 软删除标记 */
    private Boolean isDeleted = false;
    /** 计划生成状态（PENDING, SUCCESS, FAILED） */
    private String status;
    /** AI失败原因 */
    private String errorMsg;

    public RobotDailyPlan() {}

    public RobotDailyPlan(String robotId, String diary, LocalDate planDate, List<PlanSlot> slots) {
        this.robotId = robotId;
        this.diary = diary;
        this.planDate = planDate;
        this.slots = slots;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    // Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRobotId() { return robotId; }
    public void setRobotId(String robotId) { this.robotId = robotId; }
    public String getDiary() { return diary; }
    public void setDiary(String diary) { this.diary = diary; }
    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public List<PlanSlot> getSlots() { return slots; }
    public void setSlots(List<PlanSlot> slots) { this.slots = slots; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    /**
     * 时间段安排
     */
    public static class PlanSlot {
        /** 开始时间（如06:00） */
        private String start;
        /** 结束时间（如07:30） */
        private String end;
        /** 该时间段内的事件 */
        private List<PlanEvent> events = new ArrayList<>();

        public PlanSlot() {}
        public PlanSlot(String start, String end, List<PlanEvent> events) {
            this.start = start;
            this.end = end;
            this.events = events;
        }
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        public List<PlanEvent> getEvents() { return events; }
        public void setEvents(List<PlanEvent> events) { this.events = events; }
    }

    /**
     * 具体事件
     */
    public static class PlanEvent {
        /** 事件内容（如"早起洗漱"，"面膜不见了"） */
        private String content;
        /** 心情 (如开心, 难过, 生气, 惊恐) */
        private String mood;

        public PlanEvent() {}
        public PlanEvent(String content, String mood) {
            this.content = content;
            this.mood = mood;
        }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }
    }
} 