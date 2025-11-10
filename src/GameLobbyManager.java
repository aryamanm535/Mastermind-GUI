/* ECE422C Mastermind Multiplayer Lab
 * GameLobbyManager
 * 
 * This class manages all active games and connected players.
 * It serves as the central coordination point for the server.
 * 
 * LEARNING OBJECTIVES:
 * - Thread-safe data structures (ConcurrentHashMap)
 * - Central coordination logic
 * - JSON generation
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import java.util.*;
import java.util.concurrent.*;

public class GameLobbyManager {
    private final ConcurrentHashMap<String, ClientHandler> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> playerToGame = new ConcurrentHashMap<>();

    /**
     * TODO 1: Add Player to Lobby (10 minutes)
     * 
     * Registers a new player in the lobby.
     * 
     * Steps:
     * 1. Add the player to the players map with playerId as key
     * 
     * @param playerId The unique player ID
     * @param playerName The player's display name
     * @param handler The ClientHandler for this player
     */
    public void addPlayer(String playerId, String playerName, ClientHandler handler) {
        players.put(playerId, handler);
    }

    /**
     * TODO 2: Remove Player from Lobby (25 minutes)
     * 
     * Removes a player and handles cleanup.
     * 
     * Steps:
     * 1. Remove player from players map and store the handler
     * 2. If handler was found:
     *    a. Check if player was in a game (playerToGame map)
     *    b. If yes, remove from playerToGame map
     *    c. Get the GameSession they were in
     *    d. If session exists:
     *       - Call session.removePlayer(playerId)
     *       - Broadcast "PLAYER_LEFT:gameId:playerName" to remaining players
     * 
     * @param playerId The ID of the player to remove
     */
    public void removePlayer(String playerId) {
        //player removal with cleanup
        ClientHandler handler = players.remove(playerId);
        if (handler != null) {
            String gameId = playerToGame.remove(playerId);
            if (gameId != null) {
                GameSession session = sessions.get(gameId);
                if (session != null) {
                    session.removePlayer(playerId);
                    session.broadcast("PLAYER_LEFT:" + gameId + ":" + handler.getPlayerName(), playerId);
                }
            }
        }
    }

    /**
     * TODO 3: Create New Game (20 minutes)
     * 
     * Creates a new game session.
     * 
     * Steps:
     * 1. Generate a unique game ID: "g" + UUID.randomUUID().toString().substring(0, 8)
     * 2. Create a new GameSession with the gameId, gameName, requiredPlayers, and this manager
     * 3. Add the session to the sessions map
     * 4. Return the gameId
     * 
     * Note: The creator is NOT automatically added to the game - they must join manually
     * 
     * @param gameName The name for the game
     * @param requiredPlayers Number of players needed to start
     * @param creatorId The ID of the player creating the game
     * @return The generated game ID
     */
    public String createGame(String gameName, int requiredPlayers, String creatorId) {
        // generating game id
        String gameId = "g" + UUID.randomUUID().toString().substring(0, 8);
        // creating game session
        GameSession session = new GameSession(gameId, gameName, requiredPlayers, this);
        // adding to session map and returning gameid
        sessions.put(gameId, session);
        return gameId;
    }

    /**
     * TODO 4: Join Game (25 minutes)
     * 
     * Adds a player to an existing game.
     * 
     * Steps:
     * 1. Get the GameSession from sessions map
     * 2. Get the ClientHandler from players map
     * 3. If both exist:
     *    a. Try to add player to session: session.addPlayer(playerId, handler)
     *    b. If successful, add to playerToGame map
     *    c. Return true
     * 4. Otherwise return false
     * 
     * @param gameId The ID of the game to join
     * @param playerId The ID of the player joining
     * @return true if successfully joined, false otherwise
     */
    public boolean joinGame(String gameId, String playerId) {
        // getting session and handler
        GameSession session = sessions.get(gameId);
        ClientHandler handler = players.get(playerId);
        // attempting to add player and updating map if successful
        if (session != null && handler != null) {
            boolean added = session.addPlayer(playerId, handler);
            if (added) {
                playerToGame.put(playerId, gameId);
                return true;
            }
        }
        return false;
    }

    
    /**
     * TODO 5: Get Game Session (5 minutes)
     * 
     * Retrieves a game session by ID.
     * 
     * Steps:
     * 1. Return the session from the sessions map
     * 
     * @param gameId The game ID to look up
     * @return The GameSession, or null if not found
     */
    public GameSession getSession(String gameId) {
        return sessions.get(gameId);
    }

    /**
     * TODO 6: Remove Game Session (20 minutes)
     * 
     * Removes a completed or empty game.
     * 
     * Steps:
     * 1. Remove the session from sessions map
     * 2. If session was found:
     *    a. For each player ID in the session, remove from playerToGame map
     *    b. Broadcast updated game list to all players in lobby
     * 
     * @param gameId The ID of the game to remove
     */
    public void removeSession(String gameId) {
        // remove session
        GameSession session = sessions.remove(gameId);
        // cleanup
        if (session != null) {
            for (String pid : session.getPlayerIds()) {
                playerToGame.remove(pid);
            }
            // broadcast updated game list
            broadcastToLobby("GAME_LIST:" + getGameListJson());
        }
    }

    /**
     * TODO 7: Generate Game List JSON (45 minutes)
     * 
     * Creates a JSON array of all active games.
     * 
     * JSON format:
     * [
     *   {"id":"g12345","name":"Game1","players":2,"maxPlayers":4,"status":"Waiting"},
     *   {"id":"g67890","name":"Game2","players":4,"maxPlayers":4,"status":"In Progress"}
     * ]
     * 
     * Steps:
     * 1. Create a StringBuilder starting with "["
     * 2. Use a boolean flag to track if this is the first game (for comma placement)
     * 3. For each GameSession in sessions.values():
     *    a. If not first, append a comma
     *    b. Append "{"
     *    c. Append: "\"id\":\"" + session.getGameId() + "\","
     *    d. Append: "\"name\":\"" + session.getGameName() + "\","
     *    e. Append: "\"players\":" + session.getPlayerCount() + ","
     *    f. Append: "\"maxPlayers\":" + session.getMaxPlayers() + ","
     *    g. Append: "\"status\":\"" + session.getStatus() + "\""
     *    h. Append "}"
     *    i. Set first flag to false
     * 4. Append "]" and return the string
     * 
     * @return JSON string representing all games
     */
    public String getGameListJson() {
        // building JSON manually using stringbuilder
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (GameSession session : sessions.values()) {
            if (!first) 
                json.append(",");
            json.append("{");
            json.append("\"id\":\"").append(session.getGameId()).append("\",");
            json.append("\"name\":\"").append(session.getGameName()).append("\",");
            json.append("\"players\":").append(session.getPlayerCount()).append(",");
            json.append("\"maxPlayers\":").append(session.getMaxPlayers()).append(",");
            json.append("\"status\":\"").append(session.getStatus()).append("\"");
            json.append("}");
            first = false;
        }
        json.append("]");
        return json.toString();
    }

    /**
     * TODO 8: Broadcast to All Lobby Players (15 minutes)
     * 
     * Sends a message to all connected players.
     * 
     * Steps:
     * 1. Iterate through all ClientHandlers in the players map
     * 2. For each handler, call handler.sendMessage(message)
     * 
     * @param message The message to broadcast
     */
    public void broadcastToLobby(String message) {
        for (ClientHandler handler : players.values()) {
            handler.sendMessage(message);
        }
    }

    /**
     * Get a specific player's handler (already implemented)
     */
    public ClientHandler getPlayer(String playerId) {
        return players.get(playerId);
    }
}