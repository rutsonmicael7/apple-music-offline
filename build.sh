#!/bin/bash
# Script para compilar o projeto

echo "=== Apple Music Offline - Build Script ==="

# Verificar se tem Java
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Instale o JDK 17."
    exit 1
fi

# Verificar se tem Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME não definido"
    echo "Defina a variável ANDROID_HOME apontando para o SDK do Android"
    exit 1
fi

echo "✅ Java encontrado"
echo "✅ Android SDK encontrado"

# Build
echo ""
echo "=== Compilando ==="
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build concluído!"
    echo "APK em: app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build falhou"
    exit 1
fi
