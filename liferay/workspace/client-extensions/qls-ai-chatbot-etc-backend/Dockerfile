FROM azul/zulu-openjdk-alpine:21-latest AS build
ARG CLIENT_EXTENSION_NAME=qls-ai-chatbot-etc-backend

RUN apk add --no-cache dos2unix

WORKDIR /root/.liferay/workspace

RUN wget https://raw.githubusercontent.com/lgdd/liferay-product-info/main/releases.json

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY gradle.properties gradle.properties
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY platform.bndrun platform.bndrun

RUN dos2unix gradlew
RUN ./gradlew

COPY configs configs
COPY client-extensions/$CLIENT_EXTENSION_NAME client-extensions/$CLIENT_EXTENSION_NAME

RUN sed -i "s/\blocalhost:58081\b/chatbot-backend:58081/g" client-extensions/$CLIENT_EXTENSION_NAME/client-extension.yaml

RUN ./gradlew :client-extensions:$CLIENT_EXTENSION_NAME:build

FROM azul/zulu-openjdk-alpine:21-latest
ARG CLIENT_EXTENSION_NAME=qls-ai-chatbot-etc-backend

WORKDIR /opt/liferay/

COPY --from=build --chown=liferay:liferay /workspace/client-extensions/$CLIENT_EXTENSION_NAME/build/libs/*.jar jar-runner.jar
COPY --from=build --chown=liferay:liferay /workspace/client-extensions/$CLIENT_EXTENSION_NAME/dxp-metadata dxp-metadata
ENV LIFERAY_ROUTES_DXP=/opt/liferay/dxp-metadata

EXPOSE 58081

ENTRYPOINT ["java", "-jar", "/opt/liferay/jar-runner.jar"]