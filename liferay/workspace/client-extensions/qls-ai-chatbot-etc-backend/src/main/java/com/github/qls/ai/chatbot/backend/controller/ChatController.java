package com.github.qls.ai.chatbot.backend.controller;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.qls.ai.chatbot.backend.dto.ChatRequest;
import com.github.qls.ai.chatbot.backend.dto.ChatResponse;
import com.github.qls.ai.chatbot.backend.service.ChatService;


@RestController
public class ChatController extends BaseRestController {
	
	private static final Log _log = LogFactory.getLog(ChatController.class);

	 @Autowired
	 ChatService chatService;
	
	@PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatRequest chatRequest) throws IOException {
		log(jwt, _log);
        ChatResponse response = chatService.doRag(jwt, chatRequest);
		return response;
        		
    }
	
	@PostMapping("/chatStreaming")
    public ChatResponse chatStreaming(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatRequest chatRequest) {
		return null;
	}
	
}
