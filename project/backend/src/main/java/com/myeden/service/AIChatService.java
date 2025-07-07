package com.myeden.service;

import com.myeden.entity.ChatMessage;

public interface AIChatService {
    ChatMessage generateAIReply(ChatMessage userMessage);
} 