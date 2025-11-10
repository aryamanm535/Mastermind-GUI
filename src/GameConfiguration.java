/* ECE422C Mastermind Multiplayer Lab
 * GameConfiguration 
 * 
 * This file defines the game configuration constants.
 * NO MODIFICATIONS NEEDED - This is provided as-is.
 */

public class GameConfiguration {
    // Maximum number of guesses allowed per player
    public static final int guessNumber = 12;
    
    // Available colors (first letter of each color)
    // B=Blue, G=Green, O=Orange, P=Purple, R=Red, Y=Yellow
    public static final String[] colors = {"B", "G", "O", "P", "R", "Y"};
    
    // Number of pegs in the secret code
    public static final int pegNumber = 4;
    
    // Default server port
    public static final int DEFAULT_PORT = 8080;
}
