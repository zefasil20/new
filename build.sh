#!/bin/bash

# Simple build script for Task Tracker CLI
# This script compiles the Java source files and runs tests

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Task Tracker CLI Build Script${NC}"
echo "================================"

# Create build directories
mkdir -p build/classes/main
mkdir -p build/classes/test
mkdir -p build/test-results

# Download JUnit 5 if not present
JUNIT_VERSION="5.10.1"
JUNIT_PLATFORM_VERSION="1.10.1"
LIB_DIR="lib"

if [ ! -d "$LIB_DIR" ]; then
    echo -e "${YELLOW}Setting up JUnit 5...${NC}"
    mkdir -p $LIB_DIR
    
    # Download JUnit 5 jars
    curl -L -o $LIB_DIR/junit-jupiter-api-$JUNIT_VERSION.jar \
        "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/$JUNIT_VERSION/junit-jupiter-api-$JUNIT_VERSION.jar"
    
    curl -L -o $LIB_DIR/junit-jupiter-engine-$JUNIT_VERSION.jar \
        "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/$JUNIT_VERSION/junit-jupiter-engine-$JUNIT_VERSION.jar"
    
    curl -L -o $LIB_DIR/junit-platform-launcher-$JUNIT_PLATFORM_VERSION.jar \
        "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-launcher/$JUNIT_PLATFORM_VERSION/junit-platform-launcher-$JUNIT_PLATFORM_VERSION.jar"
    
    curl -L -o $LIB_DIR/junit-platform-engine-$JUNIT_PLATFORM_VERSION.jar \
        "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-engine/$JUNIT_PLATFORM_VERSION/junit-platform-engine-$JUNIT_PLATFORM_VERSION.jar"
    
    curl -L -o $LIB_DIR/junit-platform-commons-$JUNIT_PLATFORM_VERSION.jar \
        "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/$JUNIT_PLATFORM_VERSION/junit-platform-commons-$JUNIT_PLATFORM_VERSION.jar"
    
    curl -L -o $LIB_DIR/opentest4j-1.3.0.jar \
        "https://repo1.maven.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar"
fi

# Compile main classes
echo -e "${YELLOW}Compiling main classes...${NC}"
javac -d build/classes/main src/main/java/*.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Main classes compiled successfully${NC}"
else
    echo -e "${RED}Main compilation failed${NC}"
    exit 1
fi

# Compile test classes
echo -e "${YELLOW}Compiling test classes...${NC}"
javac -cp "build/classes/main:$LIB_DIR/*" -d build/classes/test src/test/java/*.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Test classes compiled successfully${NC}"
else
    echo -e "${RED}Test compilation failed${NC}"
    exit 1
fi

# Run tests
echo -e "${YELLOW}Running tests...${NC}"
java -cp "build/classes/main:build/classes/test:$LIB_DIR/*" \
    org.junit.platform.console.ConsoleLauncher \
    --class-path build/classes/test \
    --scan-class-path

if [ $? -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
else
    echo -e "${RED}Some tests failed${NC}"
    exit 1
fi

echo -e "${GREEN}Build completed successfully!${NC}"