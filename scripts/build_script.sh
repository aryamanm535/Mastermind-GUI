#!/bin/bash
# ECE422C - Mastermind Multiplayer Lab - Build Script
# Compiles all Java source files

echo "========================================="
echo "   Mastermind Multiplayer - Build"
echo "========================================="
echo ""

# Create bin directory if it doesn't exist
if [ ! -d "../bin" ]; then
    echo "Creating bin directory..."
    mkdir ../bin
fi

# Clean previous build
echo "Cleaning previous build..."
rm -rf ../bin/*

# Compile all source files
echo "Compiling source files..."
javac -d ../bin ../src/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build successful!"
    echo ""
    echo "To run the server: ./run-server.sh"
    echo "To run the client: ./run-client.sh"
    echo ""
else
    echo ""
    echo "✗ Build failed. Please check error messages above."
    echo ""
    exit 1
fi
