#!/bin/bash

echo "START BUILD STAGE --------------------------------"

for i in "config" "balancer" "eureka" "content" "notification" "auth" "storage"
do
  ./gradlew micro-service:$i:clean micro-services:$i:build --info
  mv micro-services/$i/build/libs/*.jar micro-services/$i/build/libs/$i.jar
done

echo "END BUILD STAGE ----------------------------------"
