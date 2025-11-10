#!/usr/bin/env bash
# Simple Maven wrapper for *nix (downloads Maven into .mvn/wrapper on first run)
MVNW_DIR="$(cd "$(dirname "$0")" && pwd)/.mvn/wrapper"
MAVEN_VER="3.9.11"
MAVEN_DIR="$MVNW_DIR/apache-maven-$MAVEN_VER"

if [ ! -x "$MAVEN_DIR/bin/mvn" ]; then
  echo "Maven not found in wrapper directory. Downloading Apache Maven $MAVEN_VER..."
  mkdir -p "$MVNW_DIR"
  ZIP="$MVNW_DIR/apache-maven-$MAVEN_VER-bin.zip"
  URL="https://dlcdn.apache.org/maven/maven-3/$MAVEN_VER/binaries/apache-maven-$MAVEN_VER-bin.zip"
  curl -L "$URL" -o "$ZIP"
  unzip -o "$ZIP" -d "$MVNW_DIR"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
