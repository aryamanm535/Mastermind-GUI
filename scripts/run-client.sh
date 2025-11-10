#!/bin/bash
# ECE422C - Mastermind Multiplayer Lab - Client Launch Script

echo "========================================="
echo "   Mastermind Client"
echo "========================================="
echo ""

# Check if bin directory exists
if [ ! -d "../bin" ]; then
    echo "Error: ../bin directory not found."
    echo "Please run ./build_script.sh first."
    exit 1
fi

echo "Starting Mastermind client..."
echo "Make sure the server is running on port 8080"
echo ""

# Run the client
cd ../bin
java MastermindApp
