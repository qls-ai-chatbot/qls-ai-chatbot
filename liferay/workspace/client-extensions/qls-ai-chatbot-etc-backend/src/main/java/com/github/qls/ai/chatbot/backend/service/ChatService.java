package com.github.qls.ai.chatbot.backend.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.qls.ai.chatbot.backend.controller.ChatController;
import com.github.qls.ai.chatbot.backend.dto.ChatRequest;
import com.github.qls.ai.chatbot.backend.dto.ChatResponse;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatService {
	private static final Log _log = LogFactory.getLog(ChatController.class);

	private final ChatLanguageModel chatLanguageModel;

	private final Resource userMessageResource;

	@Value("${com.liferay.lxc.dxp.mainDomain}")
	private String lxcDXPMainDomain;

	@Value("${com.liferay.lxc.dxp.server.protocol}")
	private String lxcDXPServerProtocol;

	@Value("${search.experiences.blueprint.external.reference.code}")
	private String sxpExternalRefCode;

	ChatService(ChatLanguageModel chatLanguageModel,
			@Value("classpath:/prompts/user-message.st") Resource userMessageResource) {
		this.chatLanguageModel = chatLanguageModel;
		this.userMessageResource = userMessageResource;
	}

	public ChatResponse doRag(Jwt jwt, ChatRequest chatRequest) throws IOException {

		_log.info("chatRequest:" + chatRequest);
		// call search API, retrieve contents
		JSONObject jsonObject = new JSONObject();
		JSONObject attributesObject = new JSONObject();

		attributesObject.put("search.empty.search", "false");
		attributesObject.put("search.experiences.scope.group.id", chatRequest.getAttributes().get("scopeGroupId"));
		attributesObject.put("search.experiences.blueprint.external.reference.code", sxpExternalRefCode);

		jsonObject.put("attributes", attributesObject);

		Map<String, List<String>> itemDatas = WebClient
				.create(String.join("", lxcDXPServerProtocol, "://", lxcDXPMainDomain, "/o/search/v1.0/search")).post()
				.uri(uriBuilder -> uriBuilder.queryParam("pageSize", "5").queryParam("nestedFields", "embedded")
						.queryParam("search", chatRequest.getUserMessage()).build())
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()).bodyValue(jsonObject.toString())
				.retrieve().bodyToMono(String.class).flatMap(this::parseAndExtractItemData)
				.defaultIfEmpty(Collections.emptyMap()).doOnNext(output -> {
					if (_log.isInfoEnabled()) {
						_log.info("Result of search: " + output);
					}
				}).block();
		// inject into user prompt template
		String template = "";
		try (Reader reader = new InputStreamReader(userMessageResource.getInputStream(), StandardCharsets.UTF_8)) {
			template = FileCopyUtils.copyToString(reader);
		}

		PromptTemplate promptTemplate = PromptTemplate.from(template);

		String informations = itemDatas.get("itemDatas").stream().collect(Collectors.joining("\n\n"));

		Map<String, Object> promptInputs = Map.ofEntries(Map.entry("userMessage", chatRequest.getUserMessage()),
				Map.entry("contents", informations));

		_log.info("promptInputs:" + promptInputs);
		UserMessage userMessage = promptTemplate.apply(promptInputs).toUserMessage();

		// call LLM with chat memory support
		String conversationId = chatRequest.getAttributes().get("conversationId");
		Assistant assistant = AiServices.builder(Assistant.class).chatLanguageModel(chatLanguageModel)
				.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(5)).build();
		String aiMessage = assistant.chat(conversationId, userMessage.singleText());
		return new ChatResponse.Builder().aiMessage(aiMessage).attribute("titles", itemDatas.get("titles"))
				.attribute("itemUrls", itemDatas.get("itemURLs")).build();

	}

	private Mono<Map<String, List<String>>> parseAndExtractItemData(String jsonString) {
		return Mono.fromCallable(() -> new JSONObject(jsonString)).flatMap(rootObject -> {
			if (!rootObject.has("items") || rootObject.getJSONArray("items").length() == 0) {
				return Mono.just(createEmptyResultMap());
			}

			JSONArray itemsArray = rootObject.getJSONArray("items");
			int itemCount = Math.min(itemsArray.length(), 5); // Limit to 5 items

			Flux<JSONObject> itemsFlux = Flux.range(0, itemCount).map(itemsArray::getJSONObject);

			Mono<List<String>> titleMono = itemsFlux.map(item -> item.optString("title", "")).collectList();

			Mono<List<String>> itemURLsMono = itemsFlux.map(item -> item.optString("itemURL", "")).collectList();

			Mono<List<String>> fieldDataMono = itemsFlux.map(this::extractFieldData).collectList();

			return Mono.zip(titleMono, itemURLsMono, fieldDataMono).map(tuple -> {
				Map<String, List<String>> resultMap = new HashMap<>();
				resultMap.put("titles", tuple.getT1());
				resultMap.put("itemURLs", tuple.getT2());
				resultMap.put("itemDatas", tuple.getT3());
				return resultMap;
			});
		});
	}

	private Map<String, List<String>> createEmptyResultMap() {
		Map<String, List<String>> emptyMap = new HashMap<>();
		emptyMap.put("titles", Collections.emptyList());
		emptyMap.put("itemURLs", Collections.emptyList());
		emptyMap.put("itemDatas", Collections.emptyList());
		return emptyMap;
	}

	private String extractFieldData(JSONObject item) {
		if (item.has("embedded")) {
			JSONObject embedded = item.getJSONObject("embedded");
			if (embedded.has("contentFields")) {
				JSONArray contentFields = embedded.getJSONArray("contentFields");
				return IntStream.range(0, contentFields.length()).mapToObj(contentFields::getJSONObject).map(field -> {
					String fieldDataHtml = field.getJSONObject("contentFieldValue").getString("data");
					return Jsoup.parse(fieldDataHtml).text();
				}).reduce((a, b) -> a + "; " + b).orElse("");
			}
		}
		return "";
	}

}
