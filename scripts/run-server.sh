#!/bin/bash
# ECE422C - Mastermind Multiplayer Lab - Server Launch Script

echo "========================================="
echo "   Mastermind Server"
echo "========================================="
echo ""

# Check if bin directory exists
if [ ! -d "../bin" ]; then
    echo "Error: ../bin directory not found."
    echo "Please run ./build_script.sh first."
    exit 1
fi

# Default port
PORT=8080

# Check for port argument
if [ $# -eq 1 ]; then
    PORT=$1
fi

echo "Starting server on port $PORT..."
echo "Press Ctrl+C to stop the server"
echo ""

# Run the server
cd ../bin
java MastermindServer $PORT
