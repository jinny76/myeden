package com.myeden.dto.coze;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coze对话响应对象
 */
public class CozeConversationResponse {
    
    /**
     * 对话数据
     */
    private ConversationData data;
    
    /**
     * 请求详细信息
     */
    private ResponseDetail detail;
    
    /**
     * 调用状态码，0表示成功
     */
    private int code = 0;
    
    /**
     * 状态信息
     */
    private String msg = "Success";

    public CozeConversationResponse() {}

    /**
     * 对话数据内部类
     */
    public static class ConversationData {
        /**
         * 对话ID
         */
        private String id;
        
        /**
         * 会话名称
         */
        private String name;
        
        /**
         * 附加信息
         */
        @JsonProperty("meta_data")
        private Object metaData;
        
        /**
         * 创建时间
         */
        @JsonProperty("created_at")
        private Long createdAt;
        
        /**
         * 创建者ID
         */
        @JsonProperty("creator_id")
        private String creatorId;
        
        /**
         * 渠道ID
         */
        @JsonProperty("connector_id")
        private String connectorId;
        
        /**
         * 更新时间
         */
        @JsonProperty("updated_at")
        private Long updatedAt;
        
        /**
         * 最后会话段ID
         */
        @JsonProperty("last_section_id")
        private String lastSectionId;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Object getMetaData() { return metaData; }
        public void setMetaData(Object metaData) { this.metaData = metaData; }
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
        public String getCreatorId() { return creatorId; }
        public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
        public String getConnectorId() { return connectorId; }
        public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
        public Long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
        public String getLastSectionId() { return lastSectionId; }
        public void setLastSectionId(String lastSectionId) { this.lastSectionId = lastSectionId; }
    }

    /**
     * 响应详情内部类
     */
    public static class ResponseDetail {
        /**
         * 日志ID
         */
        private String logid;

        public String getLogid() { return logid; }
        public void setLogid(String logid) { this.logid = logid; }
    }

    // 便捷构造方法
    public static CozeConversationResponse success(ConversationData data) {
        CozeConversationResponse response = new CozeConversationResponse();
        response.setData(data);
        response.setCode(0);
        response.setMsg("Success");
        return response;
    }
    
    public static CozeConversationResponse error(int code, String msg) {
        CozeConversationResponse response = new CozeConversationResponse();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }

    // 便捷方法获取对话ID
    public String getConversationId() {
        return data != null ? data.getId() : null;
    }

    // Getters and Setters
    public ConversationData getData() {
        return data;
    }

    public void setData(ConversationData data) {
        this.data = data;
    }

    public ResponseDetail getDetail() {
        return detail;
    }

    public void setDetail(ResponseDetail detail) {
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    @Override
    public String toString() {
        return "CozeConversationResponse{" +
                "data=" + data +
                ", detail=" + detail +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}