import React from 'react';
import ChatBot from "react-chatbotify";

import { Liferay } from '../services/liferay/liferay.js';
import { oAuthRequest, getServerUrl } from '../services/liferay/request.js';
import { events } from 'fetch-event-stream';

let oAuth2Client;

let serverURL;

try {
	oAuth2Client = Liferay.OAuth2Client.FromUserAgentApplication(
		'ai-chatbot-etc-spring-boot-oauth-application-user-agent'
	);

	serverURL = getServerUrl(oAuth2Client);
}
catch (error) {
	console.error(error);
}


const getConversationId = () => {
	const key = 'qls_ai_chatbot_conversationId';
	let conversationId = sessionStorage.getItem(key);

	if (!conversationId) {
		conversationId = crypto.randomUUID();
		sessionStorage.setItem(key, conversationId);
	}

	return conversationId;
};



const handleUserMessage = async (params) => {
	try {

		const payload = buildRequestPayload(params);

		const response = await oAuthRequest(oAuth2Client, {
			url: `${serverURL}/assistant/api/chat`,
			method: 'POST',
			body: JSON.stringify(payload)
		});

		const data = await response.json();

		let content = data.aiMessage;

		const { itemUrls, titles } = data.attributes;

		if (itemUrls.length > 0) {
			content += buildLinksHtml({ itemUrls, titles });
		}

		return content;

	} catch (error) {
		console.error('Error retrieving response from server :', error);
		return "Sorry, I couldn't get a response due to technical issue.";
	}
};

const handleUserMessageStreaming = async (params) => {

	let fullMessage = '';

	let links = [];

	try {

		const payload = buildRequestPayload(params);

		let abort = new AbortController();

		const response = await oAuthRequest(oAuth2Client, {
			url: 'http://localhost:58081/assistant/api/streamingChat',
			method: 'POST',
			body: JSON.stringify(payload),
			headers: {
				'Accept': 'text/event-stream'
			}
		});

		if (response.ok) {

			let stream = events(response);

			for await (let event of stream) {

				const data = JSON.parse(event.data);

				if (data.aiMessage !== 'Stream completed') {

					fullMessage += data.aiMessage;

					await params.streamMessage(fullMessage);

				} else {

					const { itemUrls, titles } = data.attributes;

					if (itemUrls.length > 0) {

						const linksMessage = buildLinksHtml({ itemUrls, titles });

						fullMessage += linksMessage;

						await params.streamMessage(fullMessage);
					}
				}
			}
		}

		await params.endStreamMessage();

	} catch (error) {

		console.error('Error retrieving response from server :', error);

		if (fullMessage.length > 0) {
			fullMessage += '\n';
		}

		await params.streamMessage(fullMessage + "Sorry, I couldn't get a response due to technical issue.");
		await params.endStreamMessage();
	}
};

const buildRequestPayload = (params) => {

	const message = params.userInput;

	const payload = {
		userMessage: message,
		attributes: {
			siteGroupId: Liferay.ThemeDisplay.getSiteGroupId(),
			scopeGroupId: Liferay.ThemeDisplay.getScopeGroupId(),
			userId: Liferay.ThemeDisplay.getUserId(),
			plid: Liferay.ThemeDisplay.getPlid(),
			languageId: Liferay.ThemeDisplay.getLanguageId(),
			conversationId: getConversationId()
		}
	}

	return payload;
};

const buildLinksHtml = ({ itemUrls, titles }) => {

	let linksHtml = '<div class="rcb-options-container">';

	itemUrls.forEach((url, i) => {
		const title = titles[i];
		linksHtml += `<div class="rcb-options"><a href="${url}" target="_blank" class="qls-ai-chatbot-link">${title} ↗</a></div>`
	});

	linksHtml += '</div>';

	return linksHtml;
}

function Assistant() {

	const settings = {
		header: {
			title: <div style={{ margin: '0px', fontSize: '20px', fontWeight: 'bold' }} >Ray</div>,
			avatar: '/o/qls-ai-chatbot-custom-element-frontend/images/chatbot_avatar.jpg'
		},
		chatHistory: {
			storageKey: "qls_ai_chatbot_history"
		},
		botBubble: {
			showAvatar: true,
			avatar: '/o/qls-ai-chatbot-custom-element-frontend/images/chatbot_avatar.jpg',
			simStream: true,
			dangerouslySetInnerHtml: true
		},
		fileAttachment: {
			disabled: true
		}
	};

	const flow = {
		start: {
			message: "Ask me anything!",
			path: "loop"
		},
		loop: {
			//message: (params) => handleUserMessage(params),
			message: async (params) => handleUserMessageStreaming(params),
			path: "loop"
		}
	}

	return <ChatBot settings={settings} flow={flow} />;
}

export default Assistant;
