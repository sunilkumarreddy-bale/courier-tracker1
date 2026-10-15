#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "================================================================================"
echo " 🚚 Building and Running Courier Delivery Manager Demo"
echo "================================================================================"

# If jar doesn't exist, build with Maven or direct javac
if [ ! -f "target/courier-jdbc-1.0.0.jar" ]; then
  if command -v mvn &> /dev/null; then
    mvn package
  elif [ -f "/Users/balesunilkumar/.gemini/antigravity/scratch/apache-maven-3.9.9/bin/mvn" ]; then
    /Users/balesunilkumar/.gemini/antigravity/scratch/apache-maven-3.9.9/bin/mvn package -Dmaven.repo.local=/Users/balesunilkumar/.gemini/antigravity/scratch/.m2/repository
  else
    echo "Compiling directly with javac..."
    mkdir -p target/classes
    cp -r src/main/resources/* target/classes/ 2>/dev/null || true
    javac -cp "target/dependency/*" -d target/classes src/main/java/courier/*.java
    java -cp "target/classes:target/dependency/*" courier.App --demo
    exit 0
  fi
fi

java -jar target/courier-jdbc-1.0.0.jar --demo "$@"
