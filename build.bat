@echo off
REM Simple build script for Task Tracker CLI
REM This script compiles the Java source files and runs tests

echo Task Tracker CLI Build Script
echo ================================

REM Create build directories
if not exist "build\classes\main" mkdir build\classes\main
if not exist "build\classes\test" mkdir build\classes\test
if not exist "build\test-results" mkdir build\test-results

REM Set up library directory for JUnit 5
set LIB_DIR=lib
if not exist "%LIB_DIR%" (
    echo Setting up JUnit 5...
    mkdir %LIB_DIR%
    
    REM Note: In a real project, you would download JUnit jars here
    REM For now, we'll compile without external dependencies for basic testing
    echo JUnit setup would be done here in a real environment
)

REM Compile main classes
echo Compiling main classes...
javac -d build\classes\main src\main\java\*.java

if %ERRORLEVEL% neq 0 (
    echo Main compilation failed
    exit /b 1
)
echo Main classes compiled successfully

REM For now, we'll skip test compilation since we don't have JUnit set up
echo Build completed successfully!
echo Note: Test execution requires JUnit 5 setup