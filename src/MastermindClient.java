/* ECE422C Mastermind Multiplayer Lab
 * MastermindClient
 * 
 * This class manages the client's connection to the server.
 * It handles both sending messages and receiving them asynchronously.
 * 
 * LEARNING OBJECTIVES:
 * - Client-side socket programming
 * - Asynchronous message handling with threads
 * - Callback pattern for event handling
 * 
 * ESTIMATED TIME: 2 hours
 */

import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class MastermindClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private Consumer<String> onMessage;
    private volatile boolean connected = false;
    private volatile boolean shuttingDown = false;
    private String playerId;
    private String playerName;

    /**
     * TODO 1: Connect to Server (30 minutes)
     * 
     * Establishes a connection to the game server.
     * 
     * Steps:
     * 1. Create a new Socket connecting to the host and port
     * 2. Create a PrintWriter from socket.getOutputStream() with auto-flush
     * 3. Create a BufferedReader from socket.getInputStream() wrapped in InputStreamReader
     * 4. Store the messageCallback in the onMessage field
     * 5. Set connected = true and shuttingDown = false
     * 6. Create a new Thread for listenLoop (name it "NetworkListener")
     * 7. Set the thread as daemon: thread.setDaemon(true)
     * 8. Start the thread
     * 9. Send initial handshake: send("HELLO:1")
     * 
     * @param host The server hostname or IP address
     * @param port The server port number
     * @param messageCallback Callback function to handle received messages
     * @throws IOException If connection fails
     */
    public void connect(String host, int port, Consumer<String> messageCallback) throws IOException {
        // creating socket connection
        socket = new Socket(host, port);
        // I/O streams
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // storing callbacks and setting flags
        onMessage = messageCallback;
        connected = true;
        shuttingDown = false;

        // creating and starting listener thread
        listenerThread = new Thread(this::listenLoop, "NetworkListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        //sending initial handshake
        send("HELLO:1");
    }

    /**
     * TODO 2: Message Listener Loop (45 minutes)
     * 
     * Runs in a background thread to receive messages from the server.
     * This is the heart of asynchronous network communication!
     * 
     * Steps:
     * 1. Wrap everything in a try-catch for IOException
     * 2. Create a loop that runs while connected is true
     * 3. Inside the loop:
     *    a. Read a line from the BufferedReader
     *    b. If line is null, break the loop (server disconnected)
     *    c. Store the line in a final variable for use in lambda
     *    d. Check if not shuttingDown
     *    e. Use SwingUtilities.invokeLater() to process message on GUI thread:
     *       - Inside the lambda, check onMessage != null and !shuttingDown
     *       - If checks pass, call onMessage.accept(message)
     * 4. In the catch block:
     *    a. Only handle error if connected and not shuttingDown
     *    b. Print error message to System.err
     *    c. Use SwingUtilities.invokeLater() to send "ERROR:Connection lost" to callback
     * 5. In the finally block:
     *    a. Call disconnect()
     * 
     * Why SwingUtilities.invokeLater()?
     * - Network thread can't directly update GUI components
     * - SwingUtilities.invokeLater() schedules code to run on the GUI thread
     * - This prevents threading issues and GUI freezing
     */
    private void listenLoop() {
        try {
            while (connected) {
                String line = in.readLine();
                if (line == null) break;

                final String message = line;
                if (!shuttingDown) {
                    SwingUtilities.invokeLater(() -> {
                        if (onMessage != null && !shuttingDown) {
                            onMessage.accept(message);
                        }
                    });
                }
            }
        } catch (IOException e) {
            if (connected && !shuttingDown) {
                System.err.println("Error in listenLoop: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    if (onMessage != null) 
                        onMessage.accept("ERROR:Connection lost");
                });
            }
        } finally {
            disconnect();
        }
    }

    /**
     * TODO 3: Send Message (10 minutes)
     * 
     * Sends a message to the server.
     * 
     * Steps:
     * 1. Check if out is not null and connected is true
     * 2. If checks pass, call out.println(message)
     * 
     * @param message The message to send
     */
    public void send(String message) {
        if (out != null && connected) 
            out.println(message);
    }

    /**
     * TODO 4: Disconnect from Server (25 minutes)
     * 
     * Cleanly closes the connection to the server.
     * 
     * Steps:
     * 1. Check if connected is true
     * 2. If not connected, return early
     * 3. Set connected = false and shuttingDown = true
     * 4. Try to send "DISCONNECT" message
     * 5. Sleep for 100ms to allow message to send (wrap in try-catch for InterruptedException)
     * 6. Close all resources in try-catch blocks:
     *    a. Close 'in' (BufferedReader)
     *    b. Close 'out' (PrintWriter)
     *    c. Close 'socket'
     * 7. Catch IOException for each close operation (can use one try-catch for all)
     */
    public void disconnect() {
        //check if connected
        if (!connected) 
            return;

        connected = false;
        shuttingDown = true;

        // trying to send diconnect message
        try { 
            send("DISCONNECT"); 
        } catch (Exception ignored) {}

        //sleeping to allow send
        try { 
            Thread.sleep(100); 
        } catch (InterruptedException ignored) {}

        try { 
            if (in != null)  
                in.close(); 
            if (out != null) 
                out.close(); 
            if (socket != null) 
                socket.close(); 
        } catch (IOException ignored) {}
    }

    /**
     * TODO 5: Check Connection Status (5 minutes)
     * 
     * Checks if the client is currently connected to the server.
     * 
     * Steps:
     * 1. Return true if all of the following are true:
     *    - connected is true
     *    - socket is not null
     *    - socket.isClosed() is false
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    // Provided methods - already implemented
    
    public void setMessageCallback(Consumer<String> callback) {
        this.onMessage = callback;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPlayerName() {
        return playerName;
    }
}