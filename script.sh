#!/bin/bash

CONFIG=true
BALANCER=true
EUREKA=true
CONTENT=true
EMAIL=true
AUTH=true


list=()


if [ $CONFIG ]; then
  list+=("config")
fi

if [ $BALANCER ]; then
  list+=("balancer")
fi

if [ $EUREKA ]; then
  list+=("eureka")
fi

if [ $CONTENT ]; then
  list+=("content")
fi

if [ $EMAIL ]; then
  list+=("email")
fi

if [ $AUTH ]; then
  list+=("auth")
fi

for i in "${list[@]}"
do
  ./gradlew micro-service:$i:clean --info
done

echo "${list[@]}"
