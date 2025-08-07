package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze文件上传响应对象
 */
public class CozeFileResponse {
    
    /**
     * 文件ID
     */
    private String id;
    
    /**
     * 文件名
     */
    private String filename;
    
    /**
     * 文件大小(字节)
     */
    private long bytes;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Long createdAt;
    
    /**
     * 文件用途
     */
    private String purpose;
    
    /**
     * 状态码
     */
    private int code = 0;
    
    /**
     * 状态消息
     */
    @JsonProperty("msg")
    private String message = "success";

    public CozeFileResponse() {}

    // 便捷构造方法
    public static CozeFileResponse success(String id, String filename, long bytes, String purpose) {
        CozeFileResponse response = new CozeFileResponse();
        response.setId(id);
        response.setFilename(filename);
        response.setBytes(bytes);
        response.setPurpose(purpose);
        response.setCreatedAt(System.currentTimeMillis());
        response.setCode(0);
        response.setMessage("success");
        return response;
    }
    
    public static CozeFileResponse error(int code, String message) {
        CozeFileResponse response = new CozeFileResponse();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CozeFileResponse{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", bytes=" + bytes +
                ", createdAt=" + createdAt +
                ", purpose='" + purpose + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}