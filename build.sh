#!/bin/bash

echo "=== Compilation du framework Sprint ==="

# Dossiers
SRC="src"
BIN="bin"
LIB="lib/servlet-api.jar"
JAR_NAME="sprint-framework-1.0.jar"

# Créer le dossier de sortie
mkdir -p $BIN

# Compiler
javac -cp $LIB -d $BIN $(find $SRC -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation !"
    exit 1
fi

# Créer le .jar
jar cf $JAR_NAME -C $BIN .

echo "✅ $JAR_NAME créé avec succès !"

# Copier dans test/lib/
cp $JAR_NAME ../test/lib/
echo "✅ Copié dans sprint-test-app/lib/"