package com.myeden.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeden.config.WeChatWorkProperties;
import com.myeden.dto.wechat.WeChatMessage;
import com.myeden.service.WeChatCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 企业微信消息加解密服务实现类
 */
@Service
public class WeChatCryptoServiceImpl implements WeChatCryptoService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeChatCryptoServiceImpl.class);
    
    private final WeChatWorkProperties weChatProperties;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WeChatCryptoServiceImpl(WeChatWorkProperties weChatProperties) {
        this.weChatProperties = weChatProperties;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String verifyUrl(String msgSignature, String timestamp, String nonce, String echoStr) {
        try {
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用");
                return null;
            }
            
            // 验证签名
            if (!verifySignature(msgSignature, timestamp, nonce, echoStr)) {
                logger.error("URL验证失败：签名验证不通过");
                return null;
            }
            
            // 解密echoStr
            String decrypted = decrypt(echoStr);
            logger.info("URL验证成功，解密结果长度: {}", decrypted != null ? decrypted.length() : 0);
            return decrypted;
            
        } catch (Exception e) {
            logger.error("URL验证过程中出现异常", e);
            return null;
        }
    }
    
    @Override
    public WeChatMessage decryptMessage(String msgSignature, String timestamp, String nonce, String encryptData) {
        try {
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用");
                return null;
            }
            
            // 验证签名
            if (!verifySignature(msgSignature, timestamp, nonce, encryptData)) {
                logger.error("消息解密失败：签名验证不通过");
                return null;
            }
            
            // 解密消息
            String decryptedXml = decrypt(encryptData);
            if (decryptedXml == null || decryptedXml.trim().isEmpty()) {
                logger.error("消息解密失败：解密结果为空");
                return null;
            }
            
            // 解析XML为消息对象
            WeChatMessage message = parseXmlToMessage(decryptedXml);
            logger.info("成功解密并解析微信消息: {}", message);
            return message;
            
        } catch (Exception e) {
            logger.error("解密微信消息时出现异常", e);
            return null;
        }
    }
    
    @Override
    public String encryptMessage(String replyMsg, String timestamp, String nonce) {
        try {
            if (!weChatProperties.isEnabled()) {
                logger.warn("企业微信功能未启用");
                return null;
            }
            
            // 加密回复消息
            String encrypted = encrypt(replyMsg);
            if (encrypted == null) {
                return null;
            }
            
            // 生成签名
            String signature = generateSignature(weChatProperties.getToken(), timestamp, nonce, encrypted);
            
            // 构建加密回复XML
            String encryptedReply = String.format(
                "<xml>" +
                "<Encrypt><![CDATA[%s]]></Encrypt>" +
                "<MsgSignature><![CDATA[%s]]></MsgSignature>" +
                "<TimeStamp>%s</TimeStamp>" +
                "<Nonce><![CDATA[%s]]></Nonce>" +
                "</xml>",
                encrypted, signature, timestamp, nonce
            );
            
            return encryptedReply;
            
        } catch (Exception e) {
            logger.error("加密回复消息时出现异常", e);
            return null;
        }
    }
    
    @Override
    public boolean verifySignature(String msgSignature, String timestamp, String nonce, String encryptMsg) {
        try {
            String calculatedSignature = generateSignature(weChatProperties.getToken(), timestamp, nonce, encryptMsg);
            boolean isValid = msgSignature.equals(calculatedSignature);
            
            if (!isValid) {
                logger.warn("签名验证失败 - 期望: {}, 实际: {}", calculatedSignature, msgSignature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("验证签名时出现异常", e);
            return false;
        }
    }
    
    /**
     * 解密数据
     */
    private String decrypt(String encryptData) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(weChatProperties.getEncodingAesKey() + "=");
            byte[] encrypted = Base64.getDecoder().decode(encryptData);
            
            // AES解密
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            
            // 去除填充
            byte[] content = removePadding(decrypted);
            
            // 提取消息内容（跳过随机字符串和长度信息）
            byte[] networkBytesOrder = Arrays.copyOfRange(content, 16, 20);
            int xmlLength = bytesToInt(networkBytesOrder);
            
            String xmlContent = new String(Arrays.copyOfRange(content, 20, 20 + xmlLength), StandardCharsets.UTF_8);
            String corpIdFromMsg = new String(Arrays.copyOfRange(content, 20 + xmlLength, content.length), StandardCharsets.UTF_8);
            
            // 验证corpId
            if (!weChatProperties.getCorpId().equals(corpIdFromMsg)) {
                logger.error("解密失败：corpId不匹配，期望: {}, 实际: {}", weChatProperties.getCorpId(), corpIdFromMsg);
                return null;
            }
            
            return xmlContent;
            
        } catch (Exception e) {
            logger.error("解密数据时出现异常", e);
            return null;
        }
    }
    
    /**
     * 加密数据
     */
    private String encrypt(String plainText) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(weChatProperties.getEncodingAesKey() + "=");
            
            // 生成16位随机字符串
            byte[] randomBytes = new byte[16];
            new SecureRandom().nextBytes(randomBytes);
            
            byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] corpIdBytes = weChatProperties.getCorpId().getBytes(StandardCharsets.UTF_8);
            
            // 构建待加密内容：randomBytes + textLength + text + corpId
            byte[] lengthBytes = intToBytes(textBytes.length);
            byte[] content = new byte[randomBytes.length + lengthBytes.length + textBytes.length + corpIdBytes.length];
            
            System.arraycopy(randomBytes, 0, content, 0, randomBytes.length);
            System.arraycopy(lengthBytes, 0, content, randomBytes.length, lengthBytes.length);
            System.arraycopy(textBytes, 0, content, randomBytes.length + lengthBytes.length, textBytes.length);
            System.arraycopy(corpIdBytes, 0, content, randomBytes.length + lengthBytes.length + textBytes.length, corpIdBytes.length);
            
            // 添加PKCS7填充
            byte[] padded = addPadding(content);
            
            // AES加密
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            byte[] encrypted = cipher.doFinal(padded);
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            logger.error("加密数据时出现异常", e);
            return null;
        }
    }
    
    /**
     * 生成签名
     */
    private String generateSignature(String token, String timestamp, String nonce, String encryptMsg) throws Exception {
        String[] params = {token, timestamp, nonce, encryptMsg};
        Arrays.sort(params);
        
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(param);
        }
        
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * 去除PKCS7填充
     */
    private byte[] removePadding(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1];
        if (pad < 1 || pad > 32) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }
    
    /**
     * 添加PKCS7填充
     */
    private byte[] addPadding(byte[] content) {
        int blockSize = 32;
        int contentLength = content.length;
        int padLength = blockSize - (contentLength % blockSize);
        
        byte[] padded = new byte[contentLength + padLength];
        System.arraycopy(content, 0, padded, 0, contentLength);
        
        for (int i = contentLength; i < padded.length; i++) {
            padded[i] = (byte) padLength;
        }
        
        return padded;
    }
    
    /**
     * 字节数组转整数（网络字节序）
     */
    private int bytesToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 |
               (bytes[1] & 0xFF) << 16 |
               (bytes[2] & 0xFF) << 8 |
               (bytes[3] & 0xFF);
    }
    
    /**
     * 整数转字节数组（网络字节序）
     */
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte) ((value >> 24) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) (value & 0xFF)
        };
    }
    
    /**
     * 简单解析XML为WeChatMessage对象
     */
    private WeChatMessage parseXmlToMessage(String xml) {
        try {
            WeChatMessage message = new WeChatMessage();
            
            // 解析各种XML标签
            message.setMsgType(extractXmlValue(xml, "MsgType"));
            message.setMsgId(extractXmlValue(xml, "MsgId"));
            message.setFromUserName(extractXmlValue(xml, "FromUserName"));
            message.setToUserName(extractXmlValue(xml, "ToUserName"));
            message.setAgentId(extractXmlValue(xml, "AgentID"));
            message.setContent(extractXmlValue(xml, "Content"));
            message.setEvent(extractXmlValue(xml, "Event"));
            message.setEventKey(extractXmlValue(xml, "EventKey"));
            message.setMediaId(extractXmlValue(xml, "MediaId"));
            message.setPicUrl(extractXmlValue(xml, "PicUrl"));
            message.setFormat(extractXmlValue(xml, "Format"));
            
            // 地理位置相关字段
            message.setLatitude(extractXmlValue(xml, "Latitude"));
            message.setLongitude(extractXmlValue(xml, "Longitude"));
            message.setPrecision(extractXmlValue(xml, "Precision"));
            message.setAppType(extractXmlValue(xml, "AppType"));
            
            String createTimeStr = extractXmlValue(xml, "CreateTime");
            if (createTimeStr != null && !createTimeStr.isEmpty()) {
                message.setCreateTime(Long.parseLong(createTimeStr));
            }
            
            return message;
            
        } catch (Exception e) {
            logger.error("解析XML消息时出现异常", e);
            return null;
        }
    }
    
    /**
     * 从XML中提取指定标签的值
     */
    private String extractXmlValue(String xml, String tagName) {
        try {
            String startTag = "<" + tagName + ">";
            String endTag = "</" + tagName + ">";
            String cdataStartTag = "<" + tagName + "><![CDATA[";
            String cdataEndTag = "]]></" + tagName + ">";
            
            int startIndex, endIndex;
            
            // 首先尝试CDATA格式
            if (xml.contains(cdataStartTag)) {
                startIndex = xml.indexOf(cdataStartTag);
                if (startIndex != -1) {
                    startIndex += cdataStartTag.length();
                    endIndex = xml.indexOf(cdataEndTag, startIndex);
                    if (endIndex != -1) {
                        return xml.substring(startIndex, endIndex);
                    }
                }
            }
            
            // 尝试普通格式
            startIndex = xml.indexOf(startTag);
            if (startIndex != -1) {
                startIndex += startTag.length();
                endIndex = xml.indexOf(endTag, startIndex);
                if (endIndex != -1) {
                    return xml.substring(startIndex, endIndex);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.warn("提取XML标签值时出现异常: tagName={}", tagName, e);
            return null;
        }
    }
}