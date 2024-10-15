package com.github.qls.ai.chatbot.backend.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatResponse {

	private String aiMessage;
	private Map<String, List<String>> attributes;
	
	
	private ChatResponse(Builder builder) {
        this.aiMessage = builder.aiMessage;
        this.attributes = builder.attributes;
    }
	
	public String getAiMessage() {
		return aiMessage;
	}


	public void setAiMessage(String aiMessage) {
		this.aiMessage = aiMessage;
	}
	
	public Map<String, List<String>> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, List<String>> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public String toString() {
		return String.format("ChatResponse [aiMessage=%s, attributes=%s]", aiMessage, attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(aiMessage, attributes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatResponse other = (ChatResponse) obj;
		return Objects.equals(aiMessage, other.aiMessage) && Objects.equals(attributes, other.attributes);
	}

	public static Builder builder () {
		return new Builder();
	}
	
	public static class Builder {
        private String aiMessage;
        private Map<String, List<String>> attributes = new HashMap<>();

        private Builder() {}
        
        public Builder aiMessage(String aiMsg) {
            this.aiMessage = aiMsg;
            return this;
        }

        public Builder attribute(String key, List<String> value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, List<String>> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public ChatResponse build() {
            return new ChatResponse(this);
        }
    }

}
