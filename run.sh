#!/bin/bash

mvn clean install

mvn -pl backend exec:java &
BACKEND_PID=$!

mvn -pl middleware exec:java &
MIDDLEWARE_PID=$!

cd frontend
python -m http.server 8042 &
FRONTEND_PID=$!

trap "kill $BACKEND_PID $MIDDLEWARE_PID $FRONTEND_PID" EXIT

wait

