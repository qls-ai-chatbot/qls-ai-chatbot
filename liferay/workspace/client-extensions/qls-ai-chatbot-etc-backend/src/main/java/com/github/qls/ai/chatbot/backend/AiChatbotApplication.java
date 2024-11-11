package com.github.qls.ai.chatbot.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.liferay.client.extension.util.spring.boot.ClientExtensionUtilSpringBootComponentScan;

@Import(ClientExtensionUtilSpringBootComponentScan.class)
@SpringBootApplication
public class AiChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiChatbotApplication.class, args);
	}
}
