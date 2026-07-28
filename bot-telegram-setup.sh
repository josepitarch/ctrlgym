#!/bin/bash

# Script para enviar un mensaje de prueba al bot de Telegram
# Uso: ./bot-telegram-setup.sh [mensaje_opcional]

set -e

# Cargar variables de entorno desde .env si existe
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Verificar que las variables necesarias estén definidas
if [ -z "$TELEGRAM_BOT_TOKEN" ]; then
  echo "Error: TELEGRAM_BOT_TOKEN no está definida"
  echo "Configúrala en .env o como variable de entorno"
  exit 1
fi

if [ -z "$TELEGRAM_CHAT_ID" ]; then
  echo "Error: TELEGRAM_CHAT_ID no está definida"
  echo "Configúrala en .env o como variable de entorno"
  exit 1
fi

# Mensaje por defecto o el proporcionado como argumento
MESSAGE="${1:-🧪 *Mensaje de prueba*

El bot de Telegram está configurado correctamente.

*Timestamp:* $(date '+%Y-%m-%d %H:%M:%S')}"

# URL de la API de Telegram
API_URL="https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage"

# Enviar el mensaje
echo "Enviando mensaje de prueba al chat ${TELEGRAM_CHAT_ID}..."

RESPONSE=$(curl -s -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"chat_id\": \"${TELEGRAM_CHAT_ID}\",
    \"text\": $(echo "$MESSAGE" | jq -Rs .),
    \"parse_mode\": \"Markdown\"
  }")

# Verificar la respuesta
if echo "$RESPONSE" | grep -q '"ok":true'; then
  echo "✓ Mensaje enviado correctamente"
else
  echo "✗ Error al enviar el mensaje:"
  echo "$RESPONSE" | jq .
  exit 1
fi
