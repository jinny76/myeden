package com.myeden.service.impl;

/**
 * Dify图片识别结果对象
 * 封装图片识别的文字内容、状态码和错误信息
 */
public class DifyImageResult {
    private boolean success;
    private String text;
    private String error;
    private String workflowRunId;
    private String taskId;

    public DifyImageResult() {}

    public DifyImageResult(boolean success, String text, String error) {
        this.success = success;
        this.text = text;
        this.error = error;
    }

    /** 是否识别成功 */
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    /** 识别到的文字内容 */
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    /** 错误信息（如有） */
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getWorkflowRunId() { return workflowRunId; }
    public void setWorkflowRunId(String workflowRunId) { this.workflowRunId = workflowRunId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
} 