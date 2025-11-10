/* ECE422C Mastermind Multiplayer Lab
 * GamePanel
 * 
 * This panel displays the multiplayer game board with real-time updates.
 * 
 * LEARNING OBJECTIVES:
 * - Complex GUI layouts
 * - Custom painting with Graphics2D
 * - Real-time UI updates from network messages
 * - Turn-based gameplay logic
 * 
 * ESTIMATED TIME: 2-3 hours
 * 
 * HINT: Study SinglePlayerPanel.java to understand the GUI structure!
 */

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel {
    private String myPlayerId;
    private String currentPlayerId;
    private MastermindClient client;
    private String gameId;
    private boolean isMyTurn = false;
    private List<String> currentGuess = new ArrayList<>();
    
    private JPanel secretCodePanel;
    private JPanel currentGuessPanel;
    private JButton submitBtn;
    private DefaultListModel<String> historyModel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JLabel turnLabel;
    
    // Added for guess counting
    private JLabel guessesLabel;
    private int totalGuessesAllowed = -1; 
    private int guessesMade = 0;
    
    private Map<String, Color> colorMap = new HashMap<>();

    public GamePanel(MastermindClient client, String gameId) {
        this.client = client;
        this.gameId = gameId;
        this.myPlayerId = client.getPlayerId();
        
        // Initialize color mappings
        colorMap.put("B", Color.BLUE);
        colorMap.put("G", Color.GREEN);
        colorMap.put("O", Color.ORANGE);
        colorMap.put("P", new Color(128, 0, 128));
        colorMap.put("R", Color.RED);
        colorMap.put("Y", Color.YELLOW);
        
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));
        
        // Set this panel as the message handler
        client.setMessageCallback(this::handleServerMessage);
        
        initComponents();
    }
    
    /**
     * GUI initialization provided - complex but study it to learn layouts!
     * 
     * This uses BorderLayout, BoxLayout, FlowLayout, and custom painting.
     */
    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        JPanel leftHeaderPanel = new JPanel();
        leftHeaderPanel.setLayout(new BoxLayout(leftHeaderPanel, BoxLayout.Y_AXIS));
        leftHeaderPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Game Board");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        String playerName = client.getPlayerName();
        if (playerName == null) playerName = "Unknown";
        JLabel playerLabel = new JLabel("Playing as: " + playerName);
        playerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        playerLabel.setForeground(new Color(189, 195, 199));
        
        leftHeaderPanel.add(titleLabel);
        leftHeaderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftHeaderPanel.add(playerLabel);
        
        // Guesses left label added
        guessesLabel = new JLabel("Guesses Left: --");
        guessesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        guessesLabel.setForeground(new Color(189, 195, 199));
        leftHeaderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftHeaderPanel.add(guessesLabel);
        
        turnLabel = new JLabel("Waiting...");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(new Color(243, 156, 18));
        
        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(turnLabel, BorderLayout.EAST);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel gameAreaPanel = createGameArea();
        JPanel chatPanel = createChatArea();
        
        mainPanel.add(gameAreaPanel, BorderLayout.CENTER);
        mainPanel.add(chatPanel, BorderLayout.EAST);
        
        // Bottom button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JButton backBtn = new JButton("Back to Lobby");
        backBtn.addActionListener(e -> handleBackToLobby());
        bottomPanel.add(backBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates game area - PROVIDED but study the structure!
     */
    private JPanel createGameArea() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // modified label to be clearer
        JLabel secretCodeLabel = new JLabel("Secret Code");
        secretCodeLabel.setForeground(Color.WHITE);
        panel.add(secretCodeLabel);

        secretCodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        secretCodePanel.setOpaque(false);
        setupSecretCode();
        panel.add(secretCodePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // modified label to be clearer
        JLabel yourGuessLabel = new JLabel("Your Guess");
        yourGuessLabel.setForeground(Color.WHITE);
        panel.add(yourGuessLabel);

        currentGuessPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        currentGuessPanel.setOpaque(false);
        setupCurrentGuess();
        panel.add(currentGuessPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // modified label to be clearer
        JLabel paletteLabel = new JLabel("Color Palette");
        paletteLabel.setForeground(Color.WHITE);
        panel.add(paletteLabel);

        JPanel colorPalette = createColorPalette();
        panel.add(colorPalette);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setOpaque(false);
        submitBtn = new JButton("Submit Guess");
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> handleSubmitGuess());
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> handleClearGuess());
        actionPanel.add(submitBtn);
        actionPanel.add(clearBtn);
        panel.add(actionPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // modified label to be clearer
        JLabel historyLabel = new JLabel("Your Guess History");
        historyLabel.setForeground(Color.WHITE);
        panel.add(historyLabel);

        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(400, 150));
        panel.add(historyScroll);
        
        return panel;
    }

    /**
     * Creates chat area - PROVIDED
     */
    private JPanel createChatArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // modified label to be more clear
        JLabel chatLabel = new JLabel("Game Chat & Event Log");
        chatLabel.setForeground(Color.WHITE);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 0));
        chatInputPanel.setOpaque(false);
        
        chatInput = new JTextField();
        chatInput.addActionListener(e -> handleSendChat());
        
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> handleSendChat());
        
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendBtn, BorderLayout.EAST);
        
        panel.add(chatLabel, BorderLayout.NORTH);
        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(chatInputPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createColorPalette() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setOpaque(false);
        
        for (String colorCode : GameConfiguration.colors) {
            JButton colorBtn = new JButton();
            colorBtn.setPreferredSize(new Dimension(60, 60));
            colorBtn.setBackground(colorMap.get(colorCode));
            colorBtn.setOpaque(true);
            colorBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            colorBtn.addActionListener(e -> addColorToGuess(colorCode));
            panel.add(colorBtn);
        }
        
        return panel;
    }

    private void setupSecretCode() {
        secretCodePanel.removeAll();
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            secretCodePanel.add(createPeg(Color.GRAY, 25));
        }
    }

    private void setupCurrentGuess() {
        currentGuessPanel.removeAll();
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            currentGuessPanel.add(createPeg(Color.LIGHT_GRAY, 30));
        }
    }

    /**
     * Creates a colored peg using custom painting - PROVIDED
     * Study this to learn Graphics2D!
     */
    private JPanel createPeg(Color color, int size) {
        JPanel peg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                int diameter = size * 2;
                int offset = 10;
                g2d.fillOval(offset, offset, diameter, diameter);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(offset, offset, diameter, diameter);
            }
        };
        int panelSize = size * 2 + 25;
        peg.setPreferredSize(new Dimension(panelSize, panelSize));
        peg.setMinimumSize(new Dimension(panelSize, panelSize));
        peg.setOpaque(false);
        return peg;
    }

    /**
     * TODO 1: Add Color to Guess (15 minutes)
     * 
     * Adds a selected color to the current guess.
     * 
     * Steps:
     * 1. Check if it's not your turn:
     *    - If not your turn, show message dialog "It's not your turn!" and return
     * 2. Check if current guess is not full (size < GameConfiguration.pegNumber):
     *    - Add the color to currentGuess list
     *    - Call updateCurrentGuessDisplay()
     *    - If guess is now full, enable submit button
     */
    private void addColorToGuess(String color) {
        // check if it's your turn
        if (!isMyTurn) {
            JOptionPane.showMessageDialog(this, "It's not your turn!", "Wait", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // adding color if there's room
        if (currentGuess.size() < GameConfiguration.pegNumber) {
            currentGuess.add(color);
            // updating display
            updateCurrentGuessDisplay();
            // enable submit if full
            if (currentGuess.size() == GameConfiguration.pegNumber) {
                submitBtn.setEnabled(true);
            }
        }
    }

    /**
     * TODO 2: Update Guess Display (20 minutes)
     * 
     * Refreshes the visual display of the current guess.
     * 
     * Steps:
     * 1. Remove all components from currentGuessPanel
     * 2. Loop through GameConfiguration.pegNumber times:
     *    a. If index < currentGuess.size(), get the color from currentGuess
     *    b. Otherwise use Color.LIGHT_GRAY (empty slot)
     *    c. Create a peg with that color and add to panel
     * 3. Call revalidate() and repaint() on currentGuessPanel
     */
    private void updateCurrentGuessDisplay() {
        // clearing panel
        currentGuessPanel.removeAll();

        // adding pegs for each position
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            Color color = i < currentGuess.size() ? colorMap.get(currentGuess.get(i)) : Color.LIGHT_GRAY;
            currentGuessPanel.add(createPeg(color, 30));
        }
        
        // display refresh
        currentGuessPanel.revalidate();
        currentGuessPanel.repaint();
    }

    /**
     * TODO 3: Submit Guess (20 minutes)
     * 
     * Sends the current guess to the server.
     * 
     * Steps:
     * 1. Check if guess is complete and it's your turn
     * 2. Join the colors into a single string (e.g., ["B","G","R","P"] -> "BGRP")
     * 3. Send message: "GUESS:" + gameId + ":" + guess
     * 4. Clear currentGuess list
     * 5. Update the display
     * 6. Disable submit button
     */
    private void handleSubmitGuess() {
        // validating and sending guess
        if (currentGuess.size() == GameConfiguration.pegNumber && isMyTurn) {
            String guess = String.join("", currentGuess);
            client.send("GUESS:" + gameId + ":" + guess);
            currentGuess.clear();
            updateCurrentGuessDisplay();
            submitBtn.setEnabled(false);
        }
    }

    /**
     * TODO 4: Clear Guess (10 minutes)
     * 
     * Clears the current guess.
     * 
     * Steps:
     * 1. Clear the currentGuess list
     * 2. Update the display
     * 3. Disable submit button
     */
    private void handleClearGuess() {
        // clear guess and update UI
        currentGuess.clear();
        updateCurrentGuessDisplay();
        submitBtn.setEnabled(false);
    }


    /**
     * TODO 5: Send Chat Message (15 minutes)
     * 
     * Sends a chat message to other players.
     * 
     * Steps:
     * 1. Get and trim text from chatInput
     * 2. If not empty:
     *    - Send message: "CHAT:" + gameId + ":" + message
     *    - Clear the chatInput field
     */
    private void handleSendChat() {
        //getting message from input
        String message = chatInput.getText().trim();
        
        // send if not empty and clear input field
        if (!message.isEmpty()) {
            client.send("CHAT:" + gameId + ":" + message);
            chatInput.setText("");
        }
    }

    /**
     * TODO 6: Handle Back to Lobby (10 minutes)
     * 
     * Returns to the lobby screen.
     * 
     * Steps:
     * 1. Send message: "LEAVE_GAME:" + gameId
     * 2. Navigate to lobby: MastermindApp.showLobby()
     */
    private void handleBackToLobby() {
        // sending leave message
        client.send("LEAVE_GAME:" + gameId);
        
        // navigate to lobby
        MastermindApp.showLobby();
    }  

    /**
     * TODO 7: Handle Server Messages (60 minutes)
     * 
     * Processes all game-related messages from the server.
     * This is the most complex message handler!
     * 
     * Message types to handle:
     * 
     * "GAME_JOINED:gameId:playersList"
     *   - Add chat message: "System: Players in game: " + playersList
     * 
     * "PLAYER_JOINED:gameId:playerName"
     *   - Add chat message: "System: " + playerName + " joined"
     * 
     * "GAME_STARTED:gameId:firstPlayerId"
     *   - Store the first player ID
     *   - Add chat message: "System: Game started!"
     * 
     * "TURN_UPDATE:gameId:activePlayerId"
     *   - Store current player ID
     *   - Check if it's your turn (activePlayerId equals myPlayerId)
     *   - Update turnLabel text and color:
     *     * If your turn: "Your Turn!" in green (Color 46, 204, 113)
     *     * If not: "Waiting for other player..." in orange (Color 243, 156, 18)
     * 
     * "GUESS_RESULT:gameId:playerName:guessNum:black:white"
     *   - Add to history: playerName + " - Guess #" + guessNum + ": " + black + "B " + white + "W"
     * 
     * "GAME_WON:gameId:winnerName:guessCount"
     *   - Add chat message with winner info
     *   - Show dialog: winnerName + " won in " + guessCount + " guesses!"
     * 
     * "GAME_OVER:gameId:secretCode"
     *   - Call revealSecretCode(secretCode)
     *   - Add chat message: "Game over! The code was: " + secretCode
     *   - Show dialog with the secret code
     * 
     * "CHAT_MESSAGE:gameId:senderName:message"
     *   - Add to chat: senderName + ": " + message
     * 
     * "PLAYER_LEFT:gameId:playerName"
     *   - Add chat message: "System: " + playerName + " left the game"
     * 
     * @param message The message from the server
     */
    private void handleServerMessage(String message) {
        if (message == null || message.isEmpty()) return;

        // split all tokens so we can index easily
        String[] tokens = message.split(":");
        String command = tokens[0];

        if (tokens.length < 2) 
            return;

        switch (command) {
            case "GAME_JOINED":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    addChatMessage("System: Players in game: " + tokens[2]);
                    totalGuessesAllowed = GameConfiguration.guessNumber;
                    guessesLabel.setText("Guesses Left: " + (totalGuessesAllowed - guessesMade));
                }
                break;

            case "PLAYER_JOINED":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    addChatMessage("System: " + tokens[2] + " joined");
                }
                break;

            case "GAME_STARTED":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    currentPlayerId = tokens[2].trim();
                    isMyTurn = currentPlayerId.equals(myPlayerId);
                    updateTurnLabel();
                    if (totalGuessesAllowed == -1) {
                        totalGuessesAllowed = GameConfiguration.guessNumber;
                        guessesLabel.setText("Guesses Left: " + (totalGuessesAllowed - guessesMade));
                    }
                    addChatMessage("System: Game started!");
                }
                break;

            case "TURN_UPDATE":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    currentPlayerId = tokens[2].trim();
                    isMyTurn = currentPlayerId.equals(myPlayerId);
                    updateTurnLabel();
                }
                break;

            case "GUESS_RESULT":
                if (tokens.length >= 7 && tokens[1].equals(gameId)) {
                    String playerName = tokens[2];
                    String guessNum = tokens[3];
                    String guess = tokens[4];
                    String black = tokens[5];
                    String white = tokens[6];

                    if (playerName.equals(client.getPlayerName())) {
                        guessesMade++;
                        guessesLabel.setText("Guesses Left: " + (totalGuessesAllowed - guessesMade));
                        historyModel.addElement(String.format("Round %s: %s (%sB, %sW)", guessNum, guess, black, white));
                    } else {
                        addChatMessage(String.format("System: %s guessed (%sB, %sW)", playerName, black, white));
                    }
                }
                break;

            case "GAME_WON":
                if (tokens.length >= 4 && tokens[1].equals(gameId)) {
                    String winnerName = tokens[2];
                    String guessCount = tokens[3];
                    isMyTurn = false;
                    addChatMessage("System: " + winnerName + " won in " + guessCount + " guesses!");
                    JOptionPane.showMessageDialog(this, winnerName + " won in " + guessCount + " guesses!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    turnLabel.setText("GAME OVER!");
                    turnLabel.setForeground(new Color(0, 0, 0));
                }
                break;

            case "GAME_OVER":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    String secretCode = tokens[2];
                    isMyTurn = false;
                    revealSecretCode(secretCode);
                    addChatMessage("Game over! The code was: " + secretCode);
                    JOptionPane.showMessageDialog(this, "Game over! The code was: " + secretCode, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    turnLabel.setText("GAME OVER!");
                    turnLabel.setForeground(new Color(0, 0, 0));
                }
                break;

            case "CHAT_MESSAGE":
                if (tokens.length >= 4 && tokens[1].equals(gameId)) {
                    String senderName = tokens[2];
                    // message may contain colons — rebuild it
                    String chatMsg = message.substring(command.length() + 1 + tokens[1].length() + 1 + senderName.length() + 1);
                    addChatMessage(senderName + ": " + chatMsg);
                }
                break;

            case "PLAYER_LEFT":
                if (tokens.length >= 3 && tokens[1].equals(gameId)) {
                    addChatMessage("System: " + tokens[2] + " left the game");
                }
                break;
        }
}


    /**
     * Helper to update the turn label and submit button state
     */
    private void updateTurnLabel() {
        if (isMyTurn) {
            turnLabel.setText("Your Turn!");
            turnLabel.setForeground(new Color(46, 204, 113));
            // only enable submit if guess is full
            submitBtn.setEnabled(currentGuess.size() == GameConfiguration.pegNumber);
        } else {
            turnLabel.setText("Waiting for other player...");
            turnLabel.setForeground(new Color(243, 156, 18));
            submitBtn.setEnabled(false); // can't submit if not your turn
        }
    }

    /**
     * TODO 8: Reveal Secret Code (15 minutes)
     * 
     * Displays the secret code at game end.
     * 
     * Steps:
     * 1. Remove all components from secretCodePanel
     * 2. For each character in the code string:
     *    a. Convert char to String
     *    b. Get the color from colorMap
     *    c. Create a peg with that color
     *    d. Add to secretCodePanel
     * 3. Call revalidate() and repaint() on secretCodePanel
     */
    private void revealSecretCode(String code) {
        // clear panel
        secretCodePanel.removeAll();
        // adding colored pegs for each position
        for (int i = 0; i < code.length(); i++) {
            String colorCode = String.valueOf(code.charAt(i));
            secretCodePanel.add(createPeg(colorMap.get(colorCode), 25));
        }
        // refresh display
        secretCodePanel.revalidate();
        secretCodePanel.repaint();
    }

    /**
     * Adds a message to chat - PROVIDED
     */
    private void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
