#!/bin/bash
# ECE422C - Mastermind Multiplayer Lab - Test Runner Script

echo "========================================="
echo "   Mastermind Unit Tests"
echo "========================================="
echo ""

# Check if bin directory exists
if [ ! -d "../bin" ]; then
    echo "Error: bin directory not found."
    echo "Please run ./build_script.sh first."
    exit 1
fi

# Compile test files if needed
echo "Compiling test files..."
javac -d ../bin -cp ../bin ../src/GameStateTest.java

if [ $? -ne 0 ]; then
    echo ""
    echo "✗ Test compilation failed."
    exit 1
fi

echo ""
echo "Running GameState tests..."
echo ""

# Run the tests
cd ../bin
java GameStateTest

exit $?
