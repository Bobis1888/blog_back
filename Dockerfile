#FROM gradle:8.7-jdk17-alpine as builder
#ARG APP_NAME=app

#ADD . /home/gradle/back
#WORKDIR /home/gradle/back

#RUN gradle clean micro-services:${APP_NAME}:build --info

FROM openjdk:17.0.2-slim
ARG APP_NAME=app
ENV TZ=Europe/Moscow

WORKDIR /app

#copy from builder stage
#COPY --from=builder /home/gradle/back/micro-services/${APP_NAME}/build/libs/*.jar /app/${APP_NAME}.jar

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl jq

ADD micro-services/${APP_NAME}/build/libs/*.jar /app/${APP_NAME}.jar

CMD java -jar ${APP_NAME}.jar
