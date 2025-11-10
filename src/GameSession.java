/* ECE422C Mastermind Multiplayer Lab
 * GameSession
 * 
 * This class represents a single game instance with multiple players.
 * It manages turn-taking, guess evaluation, and game state.
 * 
 * LEARNING OBJECTIVES:
 * - Concurrent access control with locks
 * - Game state management
 * - Turn-based logic implementation
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import java.util.*;
import java.util.concurrent.locks.*;

public class GameSession {
    private final String gameId;
    private final String gameName;
    private final int requiredPlayers;
    private final GameLobbyManager lobby;
    private final ReentrantLock lock = new ReentrantLock();
    
    private final Map<String, ClientHandler> players = new LinkedHashMap<>();
    private final Map<String, Integer> guessCount = new HashMap<>();
    private final List<String> turnOrder = new ArrayList<>();
    private final Map<String, String> playerNames = new HashMap<>();
    
    private GameState gameState;
    private String secretCode;
    private int currentTurnIndex = 0;
    private int totalGuessesMade = 0; 
    private String status = "Waiting";
    private boolean started = false;

    public GameSession(String gameId, String gameName, int requiredPlayers, GameLobbyManager lobby) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.requiredPlayers = requiredPlayers;
        this.lobby = lobby;
    }

    /**
     * TODO 1: Add Player to Game (20 minutes)
     * 
     * Adds a player to this game session if there's room and game hasn't started.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if game is full (players.size() >= requiredPlayers) or already started
     * 3. If full or started, release lock and return false
     * 4. Add player to players map: players.put(playerId, handler)
     * 5. Initialize guess count: guessCount.put(playerId, 0)
     * 6. Add to turn order: turnOrder.add(playerId)
     * 7. Store player name: playerNames.put(playerId, handler.getPlayerName())
     * 8. Broadcast updated game list to lobby
     * 9. Release lock and return true
     * 
     * @param playerId The unique ID of the player
     * @param handler The ClientHandler for this player
     * @return true if player was added successfully, false otherwise
     */
    public boolean addPlayer(String playerId, ClientHandler handler) {
        lock.lock();
        try {
            // player add logic
            if (players.size() >= requiredPlayers || started) {
                return false;
            }
            
            players.put(playerId, handler);
            playerNames.put(playerId, handler.getPlayerName());
            guessCount.put(playerId, 0);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 2: Remove Player from Game (15 minutes)
     * 
     * Removes a player from the game session.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Remove from all data structures (players, guessCount, turnOrder, playerNames)
     * 3. If game is now empty, call lobby.removeSession(gameId)
     * 4. Otherwise, if game has started and currentTurnIndex is out of bounds, reset to 0
     * 5. Broadcast updated game list
     * 6. Release the lock
     * 
     * @param playerId The ID of the player to remove
     */
    public void removePlayer(String playerId) {
        lock.lock();
        try {
            // implementing player removal logic
            players.remove(playerId);
            playerNames.remove(playerId);
            guessCount.remove(playerId);
            
            if (players.isEmpty()) {
                lobby.removeSession(gameId);
                return; // no one is left to notify
            }
            
            if (started) {
                // adjusting the turn order if the game is started
                String currentTurnPlayerId = turnOrder.get(currentTurnIndex);
                turnOrder.remove(playerId);
                
                if (turnOrder.isEmpty()) {
                    // everyone left
                    lobby.removeSession(gameId);
                    return;
                }
                
                //there is player left
                if (playerId.equals(currentTurnPlayerId)) {
                    currentTurnIndex = currentTurnIndex % turnOrder.size(); // Re-calc index
                    broadcastTurnUpdate();
                } else {
                    currentTurnIndex = turnOrder.indexOf(currentTurnPlayerId);
                    if (currentTurnIndex == -1) { 
                         currentTurnIndex = 0;
                         broadcastTurnUpdate();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 3: Check if Game Can Start (10 minutes)
     * 
     * Determines if the game has enough players to start.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Return true if players.size() equals requiredPlayers AND game hasn't started
     * 3. Release the lock
     * 
     * @return true if game can start, false otherwise
     */
    public boolean canStart() {
        lock.lock();
        try {
            //game start check
            return players.size() == requiredPlayers && !started;
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 4: Start the Game (20 minutes)
     * 
     * Initializes the game and notifies all players.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if already started, if so return early
     * 3. Set started = true and status = "In Progress"
     * 4. Generate secret code: SecretCodeGenerator.getInstance().getNewSecretCode()
     * 5. Create GameState: new GameState(secretCode)
     * 6. If turnOrder is not empty:
     *    a. Get first player: turnOrder.get(0)
     *    b. Broadcast "GAME_STARTED:gameId:firstPlayer" to all players
     *    c. Broadcast "TURN_UPDATE:gameId:firstPlayer" to all players
     * 7. Update lobby's game list
     * 8. Release the lock
     */
    public void startGame() {
        lock.lock();
        try {
            if (started) 
                return; // don't start twice 
            
            started = true;
            status = "In Progress";
            
            // generate secret code and game state
            secretCode = SecretCodeGenerator.getInstance().getNewSecretCode();
            gameState = new GameState(secretCode);
            System.out.println("[GameSession " + gameId + "] Code: " + secretCode);
            
            // populate turn order
            turnOrder.addAll(players.keySet());
            currentTurnIndex = 0;
            if (turnOrder.isEmpty()) {
                System.err.println("GameSession " + gameId + " - No players to start the game.");
                return;
            }
            String firstPlayerId = turnOrder.get(currentTurnIndex);
            
            // Broadcast
            broadcast("GAME_STARTED:" + gameId + ":" + firstPlayerId, null);
            broadcastTurnUpdate();
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 5: Process Player Guess (45 minutes)
     * 
     * Evaluates a player's guess and updates game state.
     * 
     * This is the most complex method - handle it carefully!
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if game has started, if not send "ERROR:Game not started" and return
     * 3. Get current player from turnOrder.get(currentTurnIndex)
     * 4. Verify it's this player's turn, if not send "ERROR:Not your turn" and return
     * 5. Validate the guess using isValidGuess(), if invalid send error and return
     * 6. Evaluate guess: gameState.evaluateGuess(guess) returns int[]{black, white}
     * 7. Increment and store guess count for this player
     * 8. Broadcast result: "GUESS_RESULT:gameId:playerName:guessNum:black:white"
     * 9. Check if player won (black pegs == GameConfiguration.pegNumber):
     *    a. Set status = "Finished"
     *    b. Broadcast "GAME_WON:gameId:winnerName:guessCount"
     *    c. Broadcast "GAME_OVER:gameId:secretCode"
     *    d. Release lock and return
     * 10. Check if player is out of guesses (>= GameConfiguration.guessNumber):
     *    a. Advance to next turn
     *    b. Check if all players are out of guesses
     *    c. If yes, set status = "Finished" and broadcast "GAME_OVER:gameId:secretCode"
     * 11. Otherwise, just advance to next turn
     * 12. Release the lock
     * 
     * @param playerId The ID of the player making the guess
     * @param guess The guess string (e.g., "BGRP")
     */
    public void processGuess(String playerId, String guess) {
        lock.lock();
        try {
            // check if game started
            if (!started) {
                players.get(playerId).sendMessage("ERROR:Game has not started.");
                return;
            }
            //check if it's current player's turn
            if (!playerId.equals(turnOrder.get(currentTurnIndex))) {
                players.get(playerId).sendMessage("ERROR:It's not your turn.");
                return;
            }
            //validate guess
            if (!isValidGuess(guess)) {
                players.get(playerId).sendMessage("ERROR:Invalid guess format.");
                return;
            }

            // evaluate guess
            int[] result = gameState.evaluateGuess(guess);
            int blackPegs = result[0];
            int whitePegs = result[1];
            
            // Update counts
            totalGuessesMade++;
            int playerGuessNum = guessCount.get(playerId) + 1;
            guessCount.put(playerId, playerGuessNum);
            
            String playerName = playerNames.get(playerId);

            // Broadcast result
            String resultMsg = String.format("GUESS_RESULT:%s:%s:%d:%s:%d:%d", gameId, playerName, playerGuessNum, guess, blackPegs, whitePegs);
            broadcast(resultMsg, null);
            
            // check win 
            if (blackPegs == GameConfiguration.pegNumber) {
                status = "Finished";
                started = false;
                broadcast("GAME_WON:" + gameId + ":" + playerName + ":" + playerGuessNum, null);
                return;
            }
            
            // check loss
            int maxGuesses = GameConfiguration.guessNumber * requiredPlayers;
            if (totalGuessesMade >= maxGuesses) {
                status = "Finished";
                started = false;
                broadcast("GAME_OVER:" + gameId + ":" + secretCode, null);
                return;
            }
            
            // advance turn
            advanceTurn();

        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 6: Advance Turn (15 minutes)
     * 
     * Moves to the next player's turn.
     * 
     * Steps:
     * 1. Increment currentTurnIndex using modulo: (currentTurnIndex + 1) % turnOrder.size()
     * 2. Get the next player ID from turnOrder
     * 3. Broadcast "TURN_UPDATE:gameId:nextPlayerId" to all players
     * 
     * Note: This method should be called while holding the lock
     */
    private void advanceTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        broadcastTurnUpdate();
    }
    
    /**
     * Helper to broadcast the turn update
     */
    private void broadcastTurnUpdate() {
        String nextPlayerId = turnOrder.get(currentTurnIndex);
        broadcast("TURN_UPDATE:" + gameId + ":" + nextPlayerId, null);
    }

    /**
     * TODO 7: Validate Guess Format (20 minutes)
     * 
     * Checks if a guess string is valid.
     * 
     * A valid guess must:
     * - Not be null
     * - Have length equal to GameConfiguration.pegNumber
     * - Contain only valid color codes from GameConfiguration.colors
     * 
     * Steps:
     * 1. Check if guess is null or wrong length
     * 2. For each character in the guess:
     *    a. Check if it matches any color in GameConfiguration.colors
     *    b. If no match found, return false
     * 3. If all checks pass, return true
     * 
     * @param guess The guess string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidGuess(String guess) {
        if (guess == null || guess.length() != GameConfiguration.pegNumber) {
            return false;
        }
        
        // validation of colors
        Set<String> validColors = new HashSet<>(Arrays.asList(GameConfiguration.colors));
        for (char c : guess.toCharArray()) {
            if (!validColors.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * TODO 8: Broadcast Message (10 minutes)
     * 
     * Sends a message to all players in the game, optionally excluding one.
     * 
     * Steps:
     * 1. Iterate through all entries in the players map
     * 2. For each entry, check if we should exclude this player
     * 3. If not excluded, call handler.sendMessage(message)
     * 
     * @param message The message to broadcast
     * @param excludePlayerId Player ID to exclude, or null to send to all
     */
    public void broadcast(String message, String excludePlayerId) {
        System.out.println("Broadcast " + gameId + " - " + message);
        for (Map.Entry<String, ClientHandler> entry : players.entrySet()) {
            if (excludePlayerId == null || !entry.getKey().equals(excludePlayerId)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    // Getters - already implemented
    public String getGameId() { return gameId; }
    public String getGameName() { return gameName; }
    public int getMaxPlayers() { return requiredPlayers; }
    public int getPlayerCount() { return players.size(); }
    public String getStatus() { return status; }
    
    public List<String> getPlayerNames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler handler : players.values()) {
            names.add(handler.getPlayerName());
        }
        return names;
    }
    
    public Set<String> getPlayerIds() {
        return new HashSet<>(players.keySet());
    }
}