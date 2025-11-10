/* ECE422C Mastermind Multiplayer Lab
 * MainMenuPanel
 */

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    private MastermindClient client;

    public MainMenuPanel(MastermindClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));
        
        initComponents();
    }

    private void initComponents() {
        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("MASTERMIND");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Multiplayer Edition");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);
        titlePanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        titlePanel.add(subtitleLabel, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JButton singlePlayerBtn = createStyledButton("🎮 Single Player", new Color(52, 152, 219));
        JButton multiplayerBtn = createStyledButton("🌐 Multiplayer Online", new Color(155, 89, 182));
        JButton rulesBtn = createStyledButton("📖 Rules & Help", new Color(52, 73, 94));
        JButton quitBtn = createStyledButton("❌ Quit Game", new Color(231, 76, 60));
        
        singlePlayerBtn.addActionListener(e -> MastermindApp.showSinglePlayer());
        multiplayerBtn.addActionListener(e -> MastermindApp.showConnectionDialog());
        rulesBtn.addActionListener(e -> showRules());
        quitBtn.addActionListener(e -> {
            
            // Don't wait for disconnect, just exit immediately
            Runtime.getRuntime().halt(0);
        });
        
        buttonPanel.add(singlePlayerBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(multiplayerBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(rulesBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(quitBtn);
        
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void showRules() {
        String rules = "MASTERMIND RULES\n\n" +
                "The computer generates a secret code of 4 colored pegs.\n" +
                "Colors: Blue (B), Green (G), Orange (O), Purple (P), Red (R), Yellow (Y)\n\n" +
                "Feedback:\n" +
                "• Black peg: Correct color in correct position\n" +
                "• White peg: Correct color in wrong position\n\n" +
                "You have 12 guesses to crack the code. Good luck!";
        
        JOptionPane.showMessageDialog(this, rules, "How to Play", 
                                      JOptionPane.INFORMATION_MESSAGE);
    }
}
