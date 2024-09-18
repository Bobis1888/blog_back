#!/bin/bash

echo "START BUILD STAGE --------------------------------"

for i in "config" "balancer" "eureka" "content" "notification" "user" "storage" "subscription" "statistic"
do
  ./gradlew micro-service:$i:clean micro-services:$i:build -x test --info
  mv micro-services/$i/build/libs/*.jar micro-services/$i/build/libs/$i.jar
done

echo "END BUILD STAGE ----------------------------------"
