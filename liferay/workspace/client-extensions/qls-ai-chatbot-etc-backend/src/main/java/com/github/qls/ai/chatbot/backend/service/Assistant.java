package com.github.qls.ai.chatbot.backend.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {

	@SystemMessage(fromResource = "prompts/system-message.st")
	String chat(@MemoryId String memoryId, @UserMessage String message);

	
}
