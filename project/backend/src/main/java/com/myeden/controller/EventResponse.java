package com.myeden.controller;

/**
 * 统一事件响应对象
 * 用于REST接口统一返回格式
 */
public class EventResponse {
    /** 状态码，如200、400、500 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 返回数据 */
    private Object data;

    public EventResponse() {}
    public EventResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    /** 成功响应 */
    public static EventResponse success(Object data, String message) {
        return new EventResponse(200, message, data);
    }
    public static EventResponse success(Object data) {
        return new EventResponse(200, "操作成功", data);
    }
    public static EventResponse success() {
        return new EventResponse(200, "操作成功", null);
    }
    /** 失败响应 */
    public static EventResponse error(int code, String message) {
        return new EventResponse(code, message, null);
    }
    public static EventResponse error(String message) {
        return new EventResponse(500, message, null);
    }
} 