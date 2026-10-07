#!/bin/bash

echo "=== Compilation du framework Sprint ==="

# Dossiers
SRC="src"
BIN="bin"
SERVLET_API="lib/servlet-api.jar"
JSON_LIB="lib/json-20240303.jar"
LIB="$SERVLET_API:$JSON_LIB"
JAR_NAME="sprint-framework-1.0.jar"

# Créer le dossier de sortie
mkdir -p $BIN

# Compiler
javac -cp $LIB -parameters -d $BIN $(find $SRC -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation !"
    exit 1
fi

# Créer le .jar
jar cf $JAR_NAME -C $BIN .

echo "✅ $JAR_NAME créé avec succès !"

# Copier dans test/lib/
cp $JAR_NAME ../../test_sprint_git/lib/
cp $JSON_LIB ../../test_sprint_git/lib/
cp $SERVLET_API ../../test_sprint_git/lib/
echo "✅ Framework + json.jar + servlet-api.jar copiés dans test_sprint_git/lib/"
