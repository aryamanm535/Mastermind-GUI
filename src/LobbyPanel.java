/* ECE422C Mastermind Multiplayer Lab
 * LobbyPanel
 * 
 * This panel displays available games and allows players to create or join them.
 * 
 * LEARNING OBJECTIVES:
 * - JTable for displaying data
 * - JSON parsing (manual)
 * - Dynamic UI updates from network events
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LobbyPanel extends JPanel {
    private MastermindClient client;
    private JTable gamesTable;
    private DefaultTableModel tableModel;
    private List<GameInfo> games = new ArrayList<>();

    public LobbyPanel(MastermindClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));
        
        initComponents();
        
        // Set this panel as the message handler
        client.setMessageCallback(this::handleServerMessage);
        
        // Request game list when lobby opens
        client.send("GET_GAMES");
    }

    /**
     * GUI setup is provided - study to learn Swing components
     */
    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Game Lobby");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        String[] columnNames = {"Game Name", "Players", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        gamesTable = new JTable(tableModel);
        gamesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        gamesTable.setRowHeight(30);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(gamesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setOpaque(false);
        
        JButton createGameBtn = new JButton("Create Game");
        JButton joinGameBtn = new JButton("Join Game");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn = new JButton("Back to Menu");
        
        joinGameBtn.setEnabled(false);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            joinGameBtn.setEnabled(gamesTable.getSelectedRow() >= 0);
        });
        
        createGameBtn.addActionListener(e -> handleCreateGame());
        joinGameBtn.addActionListener(e -> handleJoinGame());
        refreshBtn.addActionListener(e -> client.send("GET_GAMES"));
        backBtn.addActionListener(e -> MastermindApp.showMainMenu());
        
        buttonPanel.add(createGameBtn);
        buttonPanel.add(joinGameBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * TODO 1: Handle Create Game (20 minutes)
     * 
     * Prompts user for game details and creates a new game.
     * 
     * Steps:
     * 1. Show input dialog: JOptionPane.showInputDialog(this, "Enter game name:", "Create Game", JOptionPane.PLAIN_MESSAGE)
     * 2. Check if gameName is not null and not empty (after trim)
     * 3. If valid, create options array: {"2", "3", "4", "5", "6", "7", "8"}
     * 4. Show selection dialog: JOptionPane.showInputDialog(this, "Select required number of players:", 
     *    "Create Game", JOptionPane.PLAIN_MESSAGE, null, options, "2")
     * 5. If requiredPlayers is not null:
     *    - Send message: "CREATE_GAME:" + gameName.trim() + ":" + requiredPlayers
     */
    private void handleCreateGame() {
        // prompt for game name
        String gameName = JOptionPane.showInputDialog(this, "Enter game name:", "Create Game", JOptionPane.PLAIN_MESSAGE);

        // validating game name
        if (gameName == null || gameName.trim().isEmpty()) {
            return;
        }

        // prompt for number of players
        String[] options = {"2", "3", "4", "5", "6", "7", "8"};
        String requiredPlayers = (String) JOptionPane.showInputDialog(this, "Select required number of players:", "Create Game",
            JOptionPane.PLAIN_MESSAGE, null, options, "2");

        // CREATE_GAME message
        if (requiredPlayers != null) {
            client.send("CREATE_GAME:" + gameName.trim() + ":" + requiredPlayers);
        }
    }

    /**
     * TODO 2: Handle Join Game (20 minutes)
     * 
     * Joins the currently selected game.
     * 
     * Steps:
     * 1. Get selected row: gamesTable.getSelectedRow()
     * 2. Check if row is valid (>= 0 and < games.size())
     * 3. Get the GameInfo from the games list at the selected index
     * 4. Check if game status is "Waiting":
     *    - If yes, send message: "JOIN_GAME:" + game.id
     *    - If no, show error dialog: "Cannot join game in progress"
     */
    private void handleJoinGame() {
        // getting selected row
        int row = gamesTable.getSelectedRow();

        // Validating selection
        if (row < 0 || row >= games.size()) 
            return;

        // checking game status and sending JOIN_GAME message
        GameInfo game = games.get(row);
        if ("Waiting".equalsIgnoreCase(game.status)) {
            client.send("JOIN_GAME:" + game.id);
        } else {
            JOptionPane.showMessageDialog(this, "Cannot join game in progress",
                "Join Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * TODO 3: Handle Server Messages (45 minutes)
     * 
     * Processes all messages from the server while in the lobby.
     * 
     * Important: Don't process messages if client is shutting down!
     * 
     * Steps:
     * 1. Check if client is not null and is connected, if not return early
     * 2. Find first ':' in message and split into command and data
     * 3. Use switch statement to handle each message type:
     * 
     * Case "GAME_LIST":
     *   - If data is not empty, call parseGameList(data)
     * 
     * Case "GAME_CREATED":
     *   - If data is not empty, send "GET_GAMES" to refresh the list
     * 
     * Case "GAME_JOINED":
     *   - Split data by ":" to get gameId (first part)
     *   - Navigate to game board: MastermindApp.showGameBoard(gameId)
     * 
     * Case "GAME_STARTED":
     *   - Split data by ":" to get gameId (first part)
     *   - Navigate to game board: MastermindApp.showGameBoard(gameId)
     * 
     * Case "ERROR":
     *   - Print error to System.err
     *   - Show error dialog with the data
     * 
     * @param message The message from the server
     */
    private void handleServerMessage(String message) {
        // checking if client is still connected
        if (client == null || !client.isConnected()) 
            return;

        // parsing message into command and data
        int colonIndex = message.indexOf(':');
        if (colonIndex == -1) 
            return;

        String command = message.substring(0, colonIndex);
        String data = message.substring(colonIndex + 1);

        // handling each method type
        switch (command) {
            case "GAME_LIST":
                if (!data.isEmpty()) 
                    parseGameList(data);
                break;
            case "GAME_CREATED":
                if (!data.isEmpty()) 
                    client.send("GET_GAMES");
                break;
            case "GAME_JOINED":
                String gameIdJoined = data.split(":")[0];
                SwingUtilities.invokeLater(() -> MastermindApp.showGameBoard(gameIdJoined));
                break;
            case "GAME_STARTED":
                String gameIdStarted = data.split(":")[0];
                SwingUtilities.invokeLater(() -> MastermindApp.showGameBoard(gameIdStarted));
                break;
            case "ERROR":
                System.err.println("Server error: " + data);
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, data, "Error", JOptionPane.ERROR_MESSAGE));
                break;
        }
    }

    /**
     * TODO 4: Parse Game List JSON (60 minutes)
     * 
     * Parses a JSON array of games and updates the table.
     * 
     * This is manual JSON parsing - a great learning exercise!
     * 
     * JSON format: [{"id":"g123","name":"Game1","players":2,"maxPlayers":4,"status":"Waiting"},...]
     * 
     * Steps:
     * 1. Clear the games list and table: games.clear() and tableModel.setRowCount(0)
     * 2. Trim the JSON string
     * 3. Check if it's an empty array "[]", if so return early
     * 4. Remove outer brackets: json = json.substring(1, json.length() - 1)
     * 5. Split by "},{"  to separate game objects
     * 6. For each gameObj string:
     *    a. Remove any remaining braces: replace("{", "").replace("}", "")
     *    b. Extract values using extractJsonValue() helper:
     *       - id = extractJsonValue(gameObj, "id")
     *       - name = extractJsonValue(gameObj, "name")
     *       - playerCount = extractJsonValue(gameObj, "players")
     *       - maxPlayers = extractJsonValue(gameObj, "maxPlayers")
     *       - status = extractJsonValue(gameObj, "status")
     *    c. Create players string: playerCount + "/" + maxPlayers
     *    d. Create GameInfo object and add to games list
     *    e. Add row to table: tableModel.addRow(new Object[]{name, players, status})
     * 
     * @param json The JSON string to parse
     */
    private void parseGameList(String json) {
        // clear existing data
        games.clear();
        tableModel.setRowCount(0);

        // handling empty array case
        json = json.trim();
        if ("[]".equals(json)) return;

        // remove outer brackets
        json = json.substring(1, json.length() - 1);

        // split into game objects
        String[] gameObjects = json.split("\\},\\{");

        // parse each game object
        for (String gameObj : gameObjects) {
            gameObj = gameObj.replace("{", "").replace("}", "");

            String id = extractJsonValue(gameObj, "id");
            String name = extractJsonValue(gameObj, "name");
            String playerCount = extractJsonValue(gameObj, "players");
            String maxPlayers = extractJsonValue(gameObj, "maxPlayers");
            String status = extractJsonValue(gameObj, "status");

            String players = playerCount + "/" + maxPlayers;

            GameInfo game = new GameInfo(id, name, players, status);
            games.add(game);
            // add to table
            tableModel.addRow(new Object[]{name, players, status});
        }
    }
    
    /**
     * Helper method to extract JSON values (PROVIDED)
     * 
     * Example: extractJsonValue("\"id\":\"g123\",\"name\":\"Game1\"", "name") returns "Game1"
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return "";
        
        startIndex += searchKey.length();
        
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        // Check if value is quoted
        boolean isQuoted = startIndex < json.length() && json.charAt(startIndex) == '"';
        if (isQuoted) {
            startIndex++;
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex == -1) return "";
            return json.substring(startIndex, endIndex);
        } else {
            // Numeric or boolean value
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) {
                return json.substring(startIndex).trim();
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

    /**
     * Inner class to store game information (PROVIDED)
     */
    static class GameInfo {
        String id, name, players, status;
        
        GameInfo(String id, String name, String players, String status) {
            this.id = id;
            this.name = name;
            this.players = players;
            this.status = status;
        }
    }
}
