/* ECE422C Mastermind Multiplayer Lab
 * MastermindServer
 */

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MastermindServer {
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final GameLobbyManager lobby = new GameLobbyManager();
    private volatile boolean running = true;

    public MastermindServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        
        while (running && !pool.isShutdown()) {
            try {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, lobby));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        pool.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = GameConfiguration.DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + 
                                   GameConfiguration.DEFAULT_PORT);
            }
        }

        MastermindServer server = new MastermindServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
