package com.github.qls.ai.chatbot.backend.content.retriever;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;

import com.liferay.petra.string.StringBundler;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

public class LiferaySearchContentRetriever implements ContentRetriever {

	private Map<String, String> attributes;
	private String lxcDXPMainDomain;
	private String lxcDXPServerProtocol;
	private Jwt jwt;
	@Override
	public List<Content> retrieve(Query query) {
		JSONObject jsonObject = new JSONObject();
		JSONObject attributesObject = new JSONObject();
        
        attributesObject.put("search.empty.search", "false");
        attributesObject.put("search.experiences.scope.group.id", attributes.get("scopeGroupId"));
        attributesObject.put("search.experiences.blueprint.external.reference.code", "semantic-test");
        
        jsonObject.put("attributes", attributesObject);
		
		WebClient.create(
				StringBundler.concat(
					lxcDXPServerProtocol, "://", lxcDXPMainDomain,
					"/o/search/v1.0/search")
			).post(
			).uri(uriBuilder -> uriBuilder
				    .queryParam("pageSize", "5")
				    .queryParam("nestedFields", "embedded")
				    .queryParam("search", query.text())
				    .build()
		    ).accept(
				MediaType.APPLICATION_JSON
			).contentType(
				MediaType.APPLICATION_JSON
			).bodyValue(
				jsonObject.toString()
			).header(
				HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
			).retrieve(
			).bodyToMono(
				String.class
			).block();			
		return null;
	}

	

}
