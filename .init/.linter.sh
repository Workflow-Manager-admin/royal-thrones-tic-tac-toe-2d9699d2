#!/bin/bash
cd /home/kavia/workspace/code-generation/royal-thrones-tic-tac-toe-2d9699d2/frontend_kotlin
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

