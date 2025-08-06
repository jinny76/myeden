package com.myeden.service;

import com.myeden.dto.wechat.WeChatMessage;

/**
 * 企业微信消息加解密服务接口
 */
public interface WeChatCryptoService {
    
    /**
     * 验证URL（首次配置回调URL时使用）
     * 
     * @param msgSignature 企业微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echoStr 随机字符串
     * @return 解密后的echoStr，用于验证URL有效性
     */
    String verifyUrl(String msgSignature, String timestamp, String nonce, String echoStr);
    
    /**
     * 解密接收到的消息
     * 
     * @param msgSignature 企业微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param encryptData 加密的消息数据
     * @return 解密后的消息对象
     */
    WeChatMessage decryptMessage(String msgSignature, String timestamp, String nonce, String encryptData);
    
    /**
     * 加密回复消息
     * 
     * @param replyMsg 要加密的回复消息XML
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return 加密后的消息
     */
    String encryptMessage(String replyMsg, String timestamp, String nonce);
    
    /**
     * 验证签名
     * 
     * @param msgSignature 企业微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param encryptMsg 加密消息
     * @return 签名是否有效
     */
    boolean verifySignature(String msgSignature, String timestamp, String nonce, String encryptMsg);
}