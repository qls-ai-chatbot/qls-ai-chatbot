package com.github.qls.ai.chatbot.backend.dto;

import java.util.Map;
import java.util.Objects;

public class ChatRequest {

	private String userMessage;
	private Map<String, String> attributes;
	
	
	@Override
	public String toString() {
		return String.format("ChatRequest [userMessage=%s, attributes=%s]", userMessage, attributes);
	}
	@Override
	public int hashCode() {
		return Objects.hash(attributes, userMessage);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatRequest other = (ChatRequest) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(userMessage, other.userMessage);
	}
	public String getUserMessage() {
		return userMessage;
	}
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	
}
