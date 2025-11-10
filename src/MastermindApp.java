/* ECE422C Mastermind Multiplayer Lab
 * Main Application
 * 
 * This is the entry point for the Swing GUI application.
 * 
 * LEARNING OBJECTIVES:
 * - Swing application lifecycle
 * - JFrame management
 * - Panel switching for navigation
 * 
 * NO MODIFICATIONS NEEDED - This is provided as-is.
 */

import javax.swing.*;
import java.awt.*;

public class MastermindApp {
    private static MastermindClient client;
    private static JFrame mainFrame;

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            client = new MastermindClient();
            mainFrame = new JFrame("Mastermind - Multiplayer Edition");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(600, 500);
            mainFrame.setLocationRelativeTo(null);
            
            // Cleanup on close
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (client != null) {
                        client.disconnect();
                    }
                }
            });
            
            showMainMenu();
            mainFrame.setVisible(true);
        });
    }

    // Navigation methods - switch between different screens
    public static void showMainMenu() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setContentPane(new MainMenuPanel(client));
            mainFrame.setSize(600, 500);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.revalidate();
        });
    }

    public static void showConnectionDialog() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setContentPane(new ConnectionPanel(client));
            mainFrame.revalidate();
        });
    }

    public static void showLobby() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setContentPane(new LobbyPanel(client));
            mainFrame.revalidate();
        });
    }

    public static void showGameBoard(String gameId) {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setContentPane(new GamePanel(client, gameId));
            mainFrame.setSize(900, 700);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.revalidate();
        });
    }

    public static void showSinglePlayer() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setContentPane(new SinglePlayerPanel());
            mainFrame.setSize(850, 750);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.revalidate();
        });
    }

    public static MastermindClient getClient() {
        return client;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }
}
