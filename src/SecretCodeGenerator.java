/* ECE422C Mastermind Multiplayer Lab
 * SecretCodeGenerator 
 * 
 * This class generates random secret codes using the Singleton pattern.
 * NO MODIFICATIONS NEEDED - This is provided as-is.
 */

import java.util.Random;

public class SecretCodeGenerator {
    // Singleton instance
    private static SecretCodeGenerator instance = new SecretCodeGenerator();
    
    public static SecretCodeGenerator getInstance() { 
        return instance; 
    }
    
    private Random randomGenerator;
    
    // Private constructor - prevents external instantiation
    private SecretCodeGenerator() {
        randomGenerator = new Random();
    }
    
    /**
     * Generates a new random secret code
     * @return A string of length pegNumber with random colors
     */
    public String getNewSecretCode() {
        String result = "";
        int index, numberOfPegs = GameConfiguration.pegNumber;
        String[] colors = GameConfiguration.colors;
        
        for (int i = 0; i < numberOfPegs; i++) {
            index = randomGenerator.nextInt(colors.length);
            result += colors[index];
        }
        return result;
    }
}
