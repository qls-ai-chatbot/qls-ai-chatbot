# AI Chatbot in Liferay DXP

This repository provides an example on how to create an AI chatbot in Liferay DXP using the RAG architecture.

## Setup

Start **txtai**:
```shell
docker compose up -d txtai
```

Start **Liferay DXP**:
```shell
cd liferay/workspace
./gradlew initBundle deploy
./bundles/tomcat/bin/startup.sh
# or
./bundles/tomcat/bin/startup.bat
```

Start **Spring Boot** (chatbot backend):
```shell
cd liferay/workspace
./gradlew :client-extensions:qls-ai-chatbot-etc-backend:bootRun
```

Start **Ollama**:
```shell
ollama run llama3.2

```
>> To install Ollama, see: https://ollama.com/
