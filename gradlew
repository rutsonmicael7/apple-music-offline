#!/bin/sh

# Gradle wrapper script
# Este é um placeholder - o Gradle real precisa ser instalado

DIR="$( cd "$( dirname "$0" )" && pwd )"

if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
    echo "Gradle wrapper não encontrado."
    echo "Instale o Gradle: https://gradle.org/install/"
    exit 1
fi
