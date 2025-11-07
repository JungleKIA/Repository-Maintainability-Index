#!/bin/bash

# Test script to verify OpenRouter API key functionality
# Tests the provided key with a simple LLM request

API_KEY="${1:-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}"

echo "==================================="
echo "OpenRouter API Key Test"
echo "==================================="
echo ""
echo "Testing key: ${API_KEY:0:15}...${API_KEY: -10}"
echo ""

# Test with a simple request
RESPONSE=$(curl -s -X POST "https://openrouter.ai/api/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_KEY" \
  -H "HTTP-Referer: https://github.com/kaicode/rmi" \
  -H "X-Title: Repository Maintainability Index" \
  -d '{
    "model": "openai/gpt-3.5-turbo",
    "messages": [
      {
        "role": "user",
        "content": "Respond with just: API key is working"
      }
    ],
    "max_tokens": 20
  }')

echo "Response:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Check for errors
if echo "$RESPONSE" | grep -q "error"; then
  echo "❌ API Key Test: FAILED"
  ERROR_MSG=$(echo "$RESPONSE" | grep -o '"message":"[^"]*"' | cut -d'"' -f4)
  echo "Error: $ERROR_MSG"
  exit 1
else
  echo "✅ API Key Test: SUCCESS"
  echo ""
  echo "Key is valid and working!"
  echo "LLM analysis will work in the application."
  exit 0
fi
