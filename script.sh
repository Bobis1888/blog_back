#!/bin/bash
echo "Run script"

cd /home/prod/back
docker compose up --build -d
