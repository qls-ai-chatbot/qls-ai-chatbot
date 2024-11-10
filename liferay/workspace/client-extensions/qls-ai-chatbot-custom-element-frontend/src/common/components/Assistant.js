import React from 'react';
import ChatBot from "react-chatbotify";

import { Liferay } from '../services/liferay/liferay.js';
import { oAuthRequest, getServerUrl } from '../services/liferay/request.js';
import { events } from 'fetch-event-stream';
import {marked} from 'marked';
import DOMPurify from 'dompurify';

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

const storageKeyContextSuffix =  '_' + Liferay.ThemeDisplay.getUserId() + '_' +  Liferay.ThemeDisplay.getScopeGroupId() ;

const getConversationId = () => {
	const key = 'qls_ai_chatbot_conversationId' + storageKeyContextSuffix ;
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

		const { itemContents, titles } = data.attributes;

		content += buildLinksHtml(data.attributes);

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

					const htmlFullMessage = md2html(fullMessage);

					await params.streamMessage(htmlFullMessage);

				} else {

					const linksMessage = buildLinksHtml(data.attributes);

					const htmlFullMessage = md2html(fullMessage) + linksMessage;

					await params.streamMessage(htmlFullMessage);
					
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

const buildLinksHtml = ({ itemContents, titles }) => {

	if(titles.length === 0) {
		return '';
	}

	let linksHtml = '<div class="rcb-options-container">';

	itemContents.forEach((content, i) => {
		const title = titles[i];
		linksHtml += `<div class="rcb-options"><a target="_blank" class="qls-ai-chatbot-link" onclick="_qls_ai_chatbot_openContent(this)" data-title="${encodeURIComponent(title)}" data-content="${encodeURIComponent(content)}">${title} ↗</a></div>`
	});

	linksHtml += '</div>';

	return linksHtml;
};

const openContent = (target) => {

	console.log({target});

	const title = target.getAttribute('data-title');

	const content = target.getAttribute('data-content');

	// https://github.com/liferay/liferay-portal/blob/294d7dd2a85d73cb6c73b8215e12b55828f47489/modules/apps/frontend-js/frontend-js-web/src/main/resources/META-INF/resources/liferay/modal/Modal.js#L351

	Liferay.Util.openModal({
		title: decodeURIComponent(title),
		bodyHTML: decodeURIComponent(content),
		centered: true,
		modal: true,
		close: true,
		zIndex: 16777271,
		size: 'full-screen'
	});
}

window._qls_ai_chatbot_openContent = openContent;

const md2html = (content) => {
	let html = marked.parse(content);
	html = DOMPurify.sanitize(html);
	return html;
};

function Assistant() {

	const settings = {
		header: {
			title: <div style={{ margin: '0px', fontSize: '20px', fontWeight: 'bold' }} >Ray</div>,
			avatar: '/o/qls-ai-chatbot-custom-element-frontend/images/chatbot_avatar.jpg'
		},
		chatHistory: {
			storageKey: "qls_ai_chatbot_history" + storageKeyContextSuffix,
			storageType: 'SESSION_STORAGE'
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
