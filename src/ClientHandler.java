/* ECE422C Mastermind Multiplayer Lab
 * ClientHandler
 * 
 * This class handles communication with a single connected client.
 * It runs in its own thread and processes messages from the client.
 * 
 * LEARNING OBJECTIVES:
 * - Thread-based network communication
 * - Protocol design and message parsing
 * - Resource cleanup and error handling
 * 
 * ESTIMATED TIME: 3-4 hours
 */

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameLobbyManager lobby;
    private PrintWriter out;
    private BufferedReader in;
    private String playerId;
    private String playerName;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, GameLobbyManager lobby) {
        this.socket = socket;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            handleClientMessages();
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * TODO 1: Setup Input/Output Streams (15 minutes)
     * 
     * Initialize the input and output streams for this client connection.
     * 
     * Steps:
     * 1. Create a PrintWriter from socket.getOutputStream() with auto-flush enabled
     * 2. Create a BufferedReader from socket.getInputStream() wrapped in InputStreamReader
     * 
     * Hint: Use the socket object that was passed in the constructor
     */
    private void setupStreams() throws IOException {
        // initializing 'out' as a PrintWriter with auto-flush
        out = new PrintWriter(socket.getOutputStream(), true);
        // initializing 'in' as a BufferedReader
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("ClientHandler: Streams initialized for " + socket.getRemoteSocketAddress());
    }


    /**
     * TODO 2: Message Processing Loop (30 minutes)
     * 
     * Create the main message processing loop that:
     * 1. Continuously reads messages from the client
     * 2. Parses each message to extract the command and data
     * 3. Routes each message to the appropriate handler method
     * 
     * Message format: "COMMAND:data"
     * Examples: "CONNECT:PlayerName", "GUESS:gameId:BGRP"
     * 
     * Steps:
     * 1. Loop while 'running' is true
     * 2. Read a line from 'in' (use readLine())
     * 3. If line is null, break the loop (client disconnected)
     * 4. Split the message on the first ':' to get command and data
     * 5. Use a switch statement to route to handler methods
     * 
     * Commands to handle:
     * - HELLO: handleHello(data)
     * - CONNECT: handleConnect(data)
     * - GET_GAMES: handleGetGames()
     * - CREATE_GAME: handleCreateGame(data)
     * - JOIN_GAME: handleJoinGame(data)
     * - LEAVE_GAME: handleLeaveGame(data)
     * - GUESS: handleGuess(data)
     * - CHAT: handleChat(data)
     * - DISCONNECT: break the loop
     */
    private void handleClientMessages() throws IOException {
        //implementing the message processing loop
        String line;
        while (running && (line = in.readLine()) != null) {
            System.out.println("ClientHandler: RAW IN: \"" + line + "\"");

            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(":", 2);
            String command = parts[0].trim();
            String data = parts.length > 1 ? parts[1] : "";

            try {
                switch (command) {
                    case "HELLO":
                        sendMessage("HELLO:" + data);
                        break;
                    case "CONNECT":
                        handleConnect(data);
                        break;
                    case "GET_GAMES":
                        handleGetGames();
                        break;
                    case "CREATE_GAME":
                        handleCreateGame(data);
                        break;
                    case "JOIN_GAME":
                        handleJoinGame(data);
                        break;
                    case "LEAVE_GAME":
                        handleLeaveGame(data);
                        break;
                    case "GUESS":
                        handleGuess(data);
                        break;
                    case "CHAT":
                        handleChat(data);
                        break;
                    case "DISCONNECT":
                        running = false;
                        break;
                    default:
                        sendMessage("ERROR:Unknown command");
                }
            } catch (Exception e) {
                sendMessage("ERROR:Exception handling command:" + command);
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO 3: Connection Protocol Handler (20 minutes)
     * 
     * Handle the initial connection handshake.
     * 
     * Steps:
     * 1. Generate a unique player ID (use "p" + System.currentTimeMillis())
     * 2. Store the player name from the data parameter
     * 3. Register this client with the lobby manager
     * 4. Send a "CONNECTED:playerId" message back to the client
     * 
     * @param playerName The name provided by the connecting client
     */
    private void handleConnect(String name) {
        // Generate unique player ID
        this.playerName = (name == null || name.isEmpty()) ? ("Player" + System.currentTimeMillis()) : name.trim();
        this.playerId = "p" + System.currentTimeMillis();

        // Store player name and add player to lobby with CONNECTED response
        try {
            lobby.addPlayer(playerId, playerName, this);
            sendMessage("CONNECTED:" + playerId);
            System.out.println("ClientHandler - Player connected: " + playerName + " (" + playerId + ")");
        } catch (Exception e) {
            sendMessage("ERROR:Failed to register player");
            e.printStackTrace();
        }
    }
    
    /**
     * TODO 4: Game List Handler (15 minutes)
     * 
     * Send the current list of available games to the client.
     * 
     * Steps:
     * 1. Get the game list JSON from the lobby manager
     * 2. Send it to the client with format "GAME_LIST:jsonData"
     */
    private void handleGetGames() {
        sendMessage("GAME_LIST:" + lobby.getGameListJson());
    }

    /**
     * TODO 5: Create Game Handler (25 minutes)
     * 
     * Handle a request to create a new game.
     * 
     * Data format: "gameName:requiredPlayers"
     * Example: "MyGame:2"
     * 
     * Steps:
     * 1. Split the data by ':' to extract gameName and requiredPlayers
     * 2. Parse requiredPlayers as an integer
     * 3. Call lobby.createGame() with the parsed parameters
     * 4. Send "GAME_CREATED:gameId" back to the client
     * 5. If any error occurs, send "ERROR:Failed to create game"
     */
    private void handleCreateGame(String data) {
        // Parsing game name and required players, creating game through lobby manager with response
        try {
            if (data == null || !data.contains(":")) {
                sendMessage("ERROR:Invalid CREATE_GAME format");
                return;
            }
            String[] parts = data.split(":", 2);
            String gameName = parts[0].trim();
            int requiredPlayers = Integer.parseInt(parts[1].trim());

            String gameId = lobby.createGame(gameName, requiredPlayers, playerId);
            sendMessage("GAME_CREATED:" + gameId);
            lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
        } catch (NumberFormatException e) {
            sendMessage("ERROR:Invalid player count");
        } catch (Exception e) {
            sendMessage("ERROR:Failed to create game");
            e.printStackTrace();
        }
    }

    /**
     * TODO 6: Join Game Handler (30 minutes)
     * 
     * Handle a request to join an existing game.
     * 
     * Data format: "gameId"
     * 
     * Steps:
     * 1. Call lobby.joinGame() with the gameId and this player's ID
     * 2. If successful:
     *    a. Get the GameSession from the lobby
     *    b. Get the list of player names in the game
     *    c. Send "GAME_JOINED:gameId:playersList" to this client
     *    d. Broadcast "PLAYER_JOINED:gameId:playerName" to other players
     *    e. Check if the game can start (session.canStart())
     *    f. If yes, call session.startGame()
     * 3. If join fails, send "ERROR:Failed to join game"
     */
    private void handleJoinGame(String gameId) {
        // Attempting to join the game and sending appropriate message
        try {
            if (!lobby.joinGame(gameId, playerId)) {
                sendMessage("ERROR:Failed to join game");
                return;
            }
            GameSession session = lobby.getSession(gameId);
            if (session == null) {
                sendMessage("ERROR:Session not found");
                return;
            }
            sendMessage("GAME_JOINED:" + gameId + ":" + String.join(",", session.getPlayerNames()));
            session.broadcast("PLAYER_JOINED:" + gameId + ":" + playerName, playerId);

            if (session.canStart()) {
                session.startGame();
                lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
            }
        } catch (Exception e) {
            sendMessage("ERROR:Failed to join game");
            e.printStackTrace();
        }
    }

    
    /**
     * TODO 7: Leave Game Handler (20 minutes)
     * 
     * Handle a request to leave a game.
     * 
     * Steps:
     * 1. Get the GameSession from the lobby
     * 2. If session exists, call session.removePlayer(playerId)
     * 3. Broadcast "PLAYER_LEFT:gameId:playerName" to remaining players
     */
    private void handleLeaveGame(String gameId) {
        //remove player and notify other players
        try {
            GameSession session = lobby.getSession(gameId);
            if (session != null) {
                session.removePlayer(playerId);
                session.broadcast("PLAYER_LEFT:" + gameId + ":" + playerName, playerId);
                lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
            } else sendMessage("ERROR:Session not found");
        } catch (Exception e) {
            sendMessage("ERROR:Failed to leave game");
            e.printStackTrace();
        }
    }

    /**
     * TODO 8: Guess Handler (15 minutes)
     * 
     * Handle a guess submission from the client.
     * 
     * Data format: "gameId:guess"
     * Example: "g12345678:BGRP"
     * 
     * Steps:
     * 1. Split the data to extract gameId and guess
     * 2. Get the GameSession from the lobby
     * 3. If session exists, call session.processGuess(playerId, guess)
     */
    private void handleGuess(String data) {
        // parse gameId and guess, and forward to game session
        try {
            String[] parts = data.split(":", 2);
            if (parts.length < 2) {
                sendMessage("ERROR:Invalid GUESS format");
                return;
            }
            GameSession session = lobby.getSession(parts[0]);
            if (session == null) {
                sendMessage("ERROR:Session not found");
                return;
            }
            session.processGuess(playerId, parts[1]);
        } catch (Exception e) {
            sendMessage("ERROR:Failed to process guess");
            e.printStackTrace();
        }
    }

    /**
     * TODO 9: Chat Handler (20 minutes)
     * 
     * Handle a chat message from the client.
     * 
     * Data format: "gameId:message"
     * 
     * Steps:
     * 1. Split the data to extract gameId and message
     * 2. Get the GameSession from the lobby
     * 3. If session exists, broadcast "CHAT_MESSAGE:gameId:playerName:message" to all players
     */
    private void handleChat(String data) {
        try {
            String[] parts = data.split(":", 2);
            if (parts.length < 2) {
                sendMessage("ERROR:Invalid CHAT format");
                return;
            }
            GameSession session = lobby.getSession(parts[0]);
            if (session == null) {
                sendMessage("ERROR:Session not found");
                return;
            }
            session.broadcast("CHAT_MESSAGE:" + parts[0] + ":" + playerName + ":" + parts[1], null);
        } catch (Exception e) {
            sendMessage("ERROR:Failed to send chat");
            e.printStackTrace();
        }
    }

    /**
     * Handles the initial HELLO message (version check)
     */
    private void handleHello(String version) {
        sendMessage("HELLO:" + version);
    }

   /**
     * TODO 10: Resource Cleanup (20 minutes)
     * 
     * Clean up resources when the client disconnects.
     * 
     * Steps:
     * 1. Set running to false
     * 2. Remove this player from the lobby manager
     * 3. Close the input stream (in)
     * 4. Close the output stream (out)
     * 5. Close the socket
     * 6. Wrap each close operation in a try-catch to handle potential IOExceptions
     */
    private void cleanup() {
        // stop the running loop
        running = false;
        // removing player from lobby
        try {
            if (playerId != null) lobby.removePlayer(playerId);
        } catch (Exception e) {
            System.err.println("Cleanup - Error removing player: " + e.getMessage());
        }
        // close all streams and socket in separate try-catch blocks
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Cleanup - Error closing input: " + e.getMessage());
        }
        try {
            if (out != null) out.close();
        } catch (Exception e) {
            System.err.println("Cleanup - Error closing output: " + e.getMessage());
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Cleanup - Error closing socket: " + e.getMessage());
        }
        System.out.println("ClientHandler - Disconnected " + playerName + " (" + playerId + ")");
}


    /**
     * Sends a message to this client
     */
    public void sendMessage(String message) {
        if (out != null) 
            out.println(message);
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
}