/* ECE422C Mastermind Multiplayer Lab
 * SinglePlayerPanel
 * 
 * This panel implements single-player mode (practice).
 * Uses the same GameState logic as multiplayer.
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SinglePlayerPanel extends JPanel {
    private GameState gameState;
    private String secretCode;
    private List<String> currentGuess = new ArrayList<>();
    private int guessesRemaining = GameConfiguration.guessNumber;
    
    private JPanel secretCodePanel;
    private JPanel currentGuessPanel;
    private JButton submitBtn;
    private DefaultListModel<String> historyModel;
    private JLabel guessesLabel;
    
    private Map<String, Color> colorMap = new HashMap<>();

    public SinglePlayerPanel() {
        // Initialize colors
        colorMap.put("B", Color.BLUE);
        colorMap.put("G", Color.GREEN);
        colorMap.put("O", Color.ORANGE);
        colorMap.put("P", new Color(128, 0, 128));
        colorMap.put("R", Color.RED);
        colorMap.put("Y", Color.YELLOW);
        
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));
        
        initGame();
        initComponents();
    }

    /**
     * Initializes a new game
     */
    private void initGame() {
        secretCode = SecretCodeGenerator.getInstance().getNewSecretCode();
        gameState = new GameState(secretCode);
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Single Player");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        guessesLabel = new JLabel("Guesses Left: " + guessesRemaining);
        guessesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        guessesLabel.setForeground(new Color(243, 156, 18));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(guessesLabel, BorderLayout.EAST);
        
        // Game area - use scrollpane to ensure everything fits
        JPanel gameAreaPanel = new JPanel();
        gameAreaPanel.setLayout(new BoxLayout(gameAreaPanel, BoxLayout.Y_AXIS));
        gameAreaPanel.setBackground(new Color(52, 73, 94));
        gameAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Secret code (hidden)
        JLabel secretLabel = new JLabel("Secret Code");
        secretLabel.setForeground(Color.WHITE);
        secretLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        secretCodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        secretCodePanel.setOpaque(false);
        secretCodePanel.setPreferredSize(new Dimension(600, 90));
        secretCodePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        setupSecretCode();
        
        // Current guess
        JLabel guessLabel = new JLabel("Your Guess");
        guessLabel.setForeground(Color.WHITE);
        guessLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentGuessPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        currentGuessPanel.setOpaque(false);
        currentGuessPanel.setPreferredSize(new Dimension(600, 90));
        currentGuessPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        setupCurrentGuess();
        
        // Color palette
        JLabel paletteLabel = new JLabel("Color Palette");
        paletteLabel.setForeground(Color.WHITE);
        paletteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel colorPalette = createColorPalette();
        colorPalette.setPreferredSize(new Dimension(600, 90));
        colorPalette.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        
        // Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        submitBtn = new JButton("Submit Guess");
        submitBtn.setEnabled(false);
        submitBtn.setPreferredSize(new Dimension(150, 35));
        submitBtn.addActionListener(e -> handleSubmitGuess());
        JButton clearBtn = new JButton("Clear");
        clearBtn.setPreferredSize(new Dimension(100, 35));
        clearBtn.addActionListener(e -> handleClearGuess());
        actionPanel.add(submitBtn);
        actionPanel.add(clearBtn);
        
        // History
        JLabel historyLabel = new JLabel("Guess History");
        historyLabel.setForeground(Color.WHITE);
        historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyList.setBackground(new Color(44, 62, 80));
        historyList.setForeground(Color.WHITE);
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(500, 180));
        historyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        // Add all components
        gameAreaPanel.add(secretLabel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        gameAreaPanel.add(secretCodePanel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameAreaPanel.add(guessLabel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        gameAreaPanel.add(currentGuessPanel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameAreaPanel.add(paletteLabel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        gameAreaPanel.add(colorPalette);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameAreaPanel.add(actionPanel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameAreaPanel.add(historyLabel);
        gameAreaPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        gameAreaPanel.add(historyScroll);
        
        // Bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JButton backBtn = new JButton("Back to Menu");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.addActionListener(e -> MastermindApp.showMainMenu());
        bottomPanel.add(backBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(gameAreaPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupSecretCode() {
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            secretCodePanel.add(createPeg(Color.GRAY, 25));
        }
    }

    private void setupCurrentGuess() {
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            currentGuessPanel.add(createPeg(Color.LIGHT_GRAY, 30));
        }
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
            colorBtn.setFocusPainted(false);
            colorBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            colorBtn.addActionListener(e -> addColorToGuess(colorCode));
            panel.add(colorBtn);
        }
        
        return panel;
    }

    private JPanel createPeg(Color color, int size) {
        JPanel peg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                int diameter = size * 2;
                int offset = 10;  // Increased offset
                g2d.fillOval(offset, offset, diameter, diameter);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(offset, offset, diameter, diameter);
            }
        };
        int panelSize = size * 2 + 25;  // Increased padding significantly
        peg.setPreferredSize(new Dimension(panelSize, panelSize));
        peg.setMinimumSize(new Dimension(panelSize, panelSize));
        peg.setOpaque(false);
        return peg;
    }

    /**
     * Adds a color to the current guess
     */
    private void addColorToGuess(String color) {
        if (currentGuess.size() < GameConfiguration.pegNumber) {
            currentGuess.add(color);
            updateCurrentGuessDisplay();
            
            if (currentGuess.size() == GameConfiguration.pegNumber) {
                submitBtn.setEnabled(true);
            }
        }
    }

    /**
     * Updates the guess display
     */
    private void updateCurrentGuessDisplay() {
        currentGuessPanel.removeAll();
        for (int i = 0; i < GameConfiguration.pegNumber; i++) {
            Color color = i < currentGuess.size() ? 
                colorMap.get(currentGuess.get(i)) : Color.LIGHT_GRAY;
            currentGuessPanel.add(createPeg(color, 30));
        }
        currentGuessPanel.revalidate();
        currentGuessPanel.repaint();
    }

    /**
     * Handles guess submission
     */
    private void handleSubmitGuess() {
        if (currentGuess.size() == GameConfiguration.pegNumber) {
            String guess = String.join("", currentGuess);
            int[] result = gameState.evaluateGuess(guess);
            int blackPegs = result[0];
            int whitePegs = result[1];
            guessesRemaining--;
            String resultText = guess + " → " + blackPegs + "B " + whitePegs + "W";
            historyModel.addElement(resultText);
            guessesLabel.setText("Guesses Left: " + guessesRemaining);
            
            if (blackPegs == GameConfiguration.pegNumber) {
                revealSecretCode();
                showGameOver(true);
                return;
            }
            
            if (guessesRemaining == 0) {
                revealSecretCode();
                showGameOver(false);
            }
            
            currentGuess.clear();
            updateCurrentGuessDisplay();
            submitBtn.setEnabled(false);
        }
    }

    private void handleClearGuess() {
        currentGuess.clear();
        updateCurrentGuessDisplay();
        submitBtn.setEnabled(false);
    }

    /**
     * Reveals the secret code (called at game end)
     */
    private void revealSecretCode() {
        secretCodePanel.removeAll();
        for (int i = 0; i < secretCode.length(); i++) {
            String colorCode = String.valueOf(secretCode.charAt(i));
            secretCodePanel.add(createPeg(colorMap.get(colorCode), 25));
        }
        secretCodePanel.revalidate();
        secretCodePanel.repaint();
    }

    /**
     * Shows game over dialog
     */
    private void showGameOver(boolean won) {
        revealSecretCode();
        
        String title = won ? "Congratulations!" : "Game Over";
        String msg = won ? 
            "You cracked the code in " + (GameConfiguration.guessNumber - guessesRemaining) + " guesses!" :
            "Out of guesses! The code was: " + secretCode;
        
        Object[] options = {"Play Again", "Main Menu"};
        int result = JOptionPane.showOptionDialog(
            this, msg, title,
            JOptionPane.YES_NO_OPTION,
            won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE,
            null, options, options[0]);
        
        if (result == 0) {
            resetGame();
        } else {
            MastermindApp.showMainMenu();
        }
    }

    /**
     * Resets the game for a new round
     */
    private void resetGame() {
        secretCode = SecretCodeGenerator.getInstance().getNewSecretCode();
        gameState = new GameState(secretCode);
        guessesRemaining = GameConfiguration.guessNumber;
        currentGuess.clear();
        historyModel.clear();
        
        secretCodePanel.removeAll();
        setupSecretCode();
        secretCodePanel.revalidate();
        secretCodePanel.repaint();
        
        updateCurrentGuessDisplay();
        guessesLabel.setText("Guesses Left: " + guessesRemaining);
        submitBtn.setEnabled(false);
        
    }
}
