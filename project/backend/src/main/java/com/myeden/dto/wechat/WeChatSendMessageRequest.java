package com.myeden.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 企业微信发送消息请求对象
 */
public class WeChatSendMessageRequest {
    
    /**
     * 指定接收消息的成员，成员ID列表（多个接收者用'|'分隔，最多支持1000个）
     * 特殊情况：指定为"@all"，则向该企业应用的全部成员发送
     */
    @JsonProperty("touser")
    private String toUser;
    
    /**
     * 指定接收消息的部门，部门ID列表，多个接收者用'|'分隔，最多支持100个
     * 当touser为"@all"时忽略本参数
     */
    @JsonProperty("toparty")
    private String toParty;
    
    /**
     * 指定接收消息的标签，标签ID列表，多个接收者用'|'分隔，最多支持100个
     * 当touser为"@all"时忽略本参数
     */
    @JsonProperty("totag")
    private String toTag;
    
    /**
     * 消息类型，此时固定为：text
     */
    @JsonProperty("msgtype")
    private String msgType;
    
    /**
     * 企业应用的id，整型。企业内部开发，可在应用的设置页面查看
     */
    @JsonProperty("agentid")
    private String agentId;
    
    /**
     * 文本消息内容
     */
    @JsonProperty("text")
    private TextContent text;
    
    /**
     * 图片消息内容
     */
    @JsonProperty("image")
    private ImageContent image;
    
    /**
     * 语音消息内容
     */
    @JsonProperty("voice")
    private VoiceContent voice;
    
    /**
     * 视频消息内容
     */
    @JsonProperty("video")
    private VideoContent video;
    
    /**
     * 文件消息内容
     */
    @JsonProperty("file")
    private FileContent file;
    
    /**
     * 图文消息内容
     */
    @JsonProperty("news")
    private NewsContent news;
    
    /**
     * Markdown消息内容
     */
    @JsonProperty("markdown")
    private MarkdownContent markdown;
    
    /**
     * 表示是否是保密消息，0表示可对外分享，1表示不能分享且内容显示水印，默认为0
     */
    @JsonProperty("safe")
    private Integer safe = 0;
    
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0。
     * 仅第三方应用需要用到，企业自建应用可以忽略。
     */
    @JsonProperty("enable_id_trans")
    private Integer enableIdTrans = 0;
    
    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    @JsonProperty("enable_duplicate_check")
    private Integer enableDuplicateCheck = 0;
    
    /**
     * 表示是否重复消息检查的时间间隔，默认1800s，最大不超过4小时
     */
    @JsonProperty("duplicate_check_interval")
    private Integer duplicateCheckInterval = 1800;
    
    public WeChatSendMessageRequest() {}
    
    public WeChatSendMessageRequest(String toUser, String msgType, String agentId, TextContent text) {
        this.toUser = toUser;
        this.msgType = msgType;
        this.agentId = agentId;
        this.text = text;
    }
    
    // Getters and Setters
    public String getToUser() {
        return toUser;
    }
    
    public void setToUser(String toUser) {
        this.toUser = toUser;
    }
    
    public String getToParty() {
        return toParty;
    }
    
    public void setToParty(String toParty) {
        this.toParty = toParty;
    }
    
    public String getToTag() {
        return toTag;
    }
    
    public void setToTag(String toTag) {
        this.toTag = toTag;
    }
    
    public String getMsgType() {
        return msgType;
    }
    
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public TextContent getText() {
        return text;
    }
    
    public void setText(TextContent text) {
        this.text = text;
    }
    
    public ImageContent getImage() {
        return image;
    }
    
    public void setImage(ImageContent image) {
        this.image = image;
    }
    
    public VoiceContent getVoice() {
        return voice;
    }
    
    public void setVoice(VoiceContent voice) {
        this.voice = voice;
    }
    
    public VideoContent getVideo() {
        return video;
    }
    
    public void setVideo(VideoContent video) {
        this.video = video;
    }
    
    public FileContent getFile() {
        return file;
    }
    
    public void setFile(FileContent file) {
        this.file = file;
    }
    
    public NewsContent getNews() {
        return news;
    }
    
    public void setNews(NewsContent news) {
        this.news = news;
    }
    
    public MarkdownContent getMarkdown() {
        return markdown;
    }
    
    public void setMarkdown(MarkdownContent markdown) {
        this.markdown = markdown;
    }
    
    public Integer getSafe() {
        return safe;
    }
    
    public void setSafe(Integer safe) {
        this.safe = safe;
    }
    
    public Integer getEnableIdTrans() {
        return enableIdTrans;
    }
    
    public void setEnableIdTrans(Integer enableIdTrans) {
        this.enableIdTrans = enableIdTrans;
    }
    
    public Integer getEnableDuplicateCheck() {
        return enableDuplicateCheck;
    }
    
    public void setEnableDuplicateCheck(Integer enableDuplicateCheck) {
        this.enableDuplicateCheck = enableDuplicateCheck;
    }
    
    public Integer getDuplicateCheckInterval() {
        return duplicateCheckInterval;
    }
    
    public void setDuplicateCheckInterval(Integer duplicateCheckInterval) {
        this.duplicateCheckInterval = duplicateCheckInterval;
    }
    
    /**
     * 文本消息内容
     */
    public static class TextContent {
        /**
         * 消息内容，最长不超过2048个字节，超过将截断（支持id转译）
         */
        @JsonProperty("content")
        private String content;
        
        public TextContent() {}
        
        public TextContent(String content) {
            this.content = content;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 图片消息内容
     */
    public static class ImageContent {
        /**
         * 图片媒体文件id，可以调用上传临时素材接口获取
         */
        @JsonProperty("media_id")
        private String mediaId;
        
        public ImageContent() {}
        
        public ImageContent(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }
    
    /**
     * 语音消息内容
     */
    public static class VoiceContent {
        /**
         * 语音文件id，可以调用上传临时素材接口获取
         */
        @JsonProperty("media_id")
        private String mediaId;
        
        public VoiceContent() {}
        
        public VoiceContent(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }
    
    /**
     * 视频消息内容
     */
    public static class VideoContent {
        /**
         * 视频媒体文件id，可以调用上传临时素材接口获取
         */
        @JsonProperty("media_id")
        private String mediaId;
        
        /**
         * 视频消息的标题，不超过128个字节，超过会自动截断
         */
        @JsonProperty("title")
        private String title;
        
        /**
         * 视频消息的描述，不超过512个字节，超过会自动截断
         */
        @JsonProperty("description")
        private String description;
        
        public VideoContent() {}
        
        public VideoContent(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public VideoContent(String mediaId, String title, String description) {
            this.mediaId = mediaId;
            this.title = title;
            this.description = description;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    /**
     * 文件消息内容
     */
    public static class FileContent {
        /**
         * 文件id，可以调用上传临时素材接口获取
         */
        @JsonProperty("media_id")
        private String mediaId;
        
        public FileContent() {}
        
        public FileContent(String mediaId) {
            this.mediaId = mediaId;
        }
        
        public String getMediaId() {
            return mediaId;
        }
        
        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }
    
    /**
     * 图文消息内容
     */
    public static class NewsContent {
        /**
         * 图文消息，一个图文消息支持1到8条图文
         */
        @JsonProperty("articles")
        private Article[] articles;
        
        public NewsContent() {}
        
        public NewsContent(Article[] articles) {
            this.articles = articles;
        }
        
        public Article[] getArticles() {
            return articles;
        }
        
        public void setArticles(Article[] articles) {
            this.articles = articles;
        }
        
        /**
         * 图文消息的一条图文信息
         */
        public static class Article {
            /**
             * 标题，不超过128个字节，超过会自动截断
             */
            @JsonProperty("title")
            private String title;
            
            /**
             * 描述，不超过512个字节，超过会自动截断
             */
            @JsonProperty("description")
            private String description;
            
            /**
             * 点击后跳转的链接
             */
            @JsonProperty("url")
            private String url;
            
            /**
             * 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图 1068*455，小图150*150。
             */
            @JsonProperty("picurl")
            private String picUrl;
            
            public Article() {}
            
            public Article(String title, String description, String url, String picUrl) {
                this.title = title;
                this.description = description;
                this.url = url;
                this.picUrl = picUrl;
            }
            
            public String getTitle() {
                return title;
            }
            
            public void setTitle(String title) {
                this.title = title;
            }
            
            public String getDescription() {
                return description;
            }
            
            public void setDescription(String description) {
                this.description = description;
            }
            
            public String getUrl() {
                return url;
            }
            
            public void setUrl(String url) {
                this.url = url;
            }
            
            public String getPicUrl() {
                return picUrl;
            }
            
            public void setPicUrl(String picUrl) {
                this.picUrl = picUrl;
            }
        }
    }
    
    /**
     * Markdown消息内容
     */
    public static class MarkdownContent {
        /**
         * markdown内容，最长不超过4096个字节，必须是utf8编码
         */
        @JsonProperty("content")
        private String content;
        
        public MarkdownContent() {}
        
        public MarkdownContent(String content) {
            this.content = content;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}