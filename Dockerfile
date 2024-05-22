FROM gradle:8.7-jdk17-alpine as builder
ARG APP_NAME=app

ADD . /project

WORKDIR /project

RUN gradle micro-services:${APP_NAME}:build --info

FROM openjdk:17.0.2-slim
ARG APP_NAME=app
WORKDIR /app

# copy from builder stage
COPY --from=builder /project/micro-services/${APP_NAME}/build/libs/*.jar /app/app.jar

CMD ["java", "-jar", "app.jar"]
