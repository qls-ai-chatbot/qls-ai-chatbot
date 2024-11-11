package com.github.qls.ai.chatbot.backend.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface StreamingAssistant {
	//@SystemMessage(fromResource = "system-message-with-suggestion.st")
	@SystemMessage(fromResource = "prompts/system-message.st")
	Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);

	
}
