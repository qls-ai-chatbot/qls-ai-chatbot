package com.github.qls.ai.chatbot.backend.controller;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.qls.ai.chatbot.backend.dto.ChatRequest;
import com.github.qls.ai.chatbot.backend.dto.ChatResponse;
import com.github.qls.ai.chatbot.backend.service.ChatService;

import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/assistant/api")
public class ChatController extends BaseRestController {
	
	private static final Log _log = LogFactory.getLog(ChatController.class);

	 @Autowired
	 ChatService chatService;
	
	@CrossOrigin
	@PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatRequest chatRequest) throws IOException {
		log(jwt, _log);
        ChatResponse response = chatService.doRag(jwt, chatRequest);
		return response;
        		
    }
	@CrossOrigin
	@PostMapping(path ="/streamingChat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamingChat(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatRequest chatRequest) throws IOException {
		return chatService.doStreamingRag(jwt, chatRequest);
	}
	
}
