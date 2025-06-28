package com.myeden.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Dify API响应模型（新版）
 * 适配新版Dify API返回结构
 * 
 * @author MyEden Team
 * @version 1.0.0
 */
public class DifyResponse {
    
    private String event;
    @JsonProperty("message_id")
    private String messageId;
    @JsonProperty("conversation_id")
    private String conversationId;
    private String mode;
    private String answer;
    private Metadata metadata;
    @JsonProperty("created_at")
    private long createdAt;
    
    // Getters and Setters
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public static class Metadata {
        private Usage usage;
        @JsonProperty("retriever_resources")
        private List<RetrieverResource> retrieverResources;
        // Getters and Setters
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
        public List<RetrieverResource> getRetrieverResources() { return retrieverResources; }
        public void setRetrieverResources(List<RetrieverResource> retrieverResources) { this.retrieverResources = retrieverResources; }
    }

    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("prompt_unit_price")
        private String promptUnitPrice;
        @JsonProperty("prompt_price_unit")
        private String promptPriceUnit;
        @JsonProperty("prompt_price")
        private String promptPrice;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("completion_unit_price")
        private String completionUnitPrice;
        @JsonProperty("completion_price_unit")
        private String completionPriceUnit;
        @JsonProperty("completion_price")
        private String completionPrice;
        @JsonProperty("total_tokens")
        private int totalTokens;
        @JsonProperty("total_price")
        private String totalPrice;
        private String currency;
        private double latency;
        // Getters and Setters
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public String getPromptUnitPrice() { return promptUnitPrice; }
        public void setPromptUnitPrice(String promptUnitPrice) { this.promptUnitPrice = promptUnitPrice; }
        public String getPromptPriceUnit() { return promptPriceUnit; }
        public void setPromptPriceUnit(String promptPriceUnit) { this.promptPriceUnit = promptPriceUnit; }
        public String getPromptPrice() { return promptPrice; }
        public void setPromptPrice(String promptPrice) { this.promptPrice = promptPrice; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        public String getCompletionUnitPrice() { return completionUnitPrice; }
        public void setCompletionUnitPrice(String completionUnitPrice) { this.completionUnitPrice = completionUnitPrice; }
        public String getCompletionPriceUnit() { return completionPriceUnit; }
        public void setCompletionPriceUnit(String completionPriceUnit) { this.completionPriceUnit = completionPriceUnit; }
        public String getCompletionPrice() { return completionPrice; }
        public void setCompletionPrice(String completionPrice) { this.completionPrice = completionPrice; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
        public String getTotalPrice() { return totalPrice; }
        public void setTotalPrice(String totalPrice) { this.totalPrice = totalPrice; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public double getLatency() { return latency; }
        public void setLatency(double latency) { this.latency = latency; }
    }

    public static class RetrieverResource {
        private int position;
        @JsonProperty("dataset_id")
        private String datasetId;
        @JsonProperty("dataset_name")
        private String datasetName;
        @JsonProperty("document_id")
        private String documentId;
        @JsonProperty("document_name")
        private String documentName;
        @JsonProperty("segment_id")
        private String segmentId;
        private double score;
        private String content;
        // Getters and Setters
        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
        public String getDatasetId() { return datasetId; }
        public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
        public String getDatasetName() { return datasetName; }
        public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        public String getSegmentId() { return segmentId; }
        public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
} 