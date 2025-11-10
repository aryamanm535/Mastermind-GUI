/* ECE422C Mastermind Multiplayer Lab
 * GameState
 * 
 * This class evaluates guesses against the secret code.
 */

public class GameState {
    private final String secretCode;
    
    public GameState(String secretCode) {
        this.secretCode = secretCode;
    }
    
    /**
     * Evaluates a guess against the secret code
     * 
     * Algorithm:
     * 1. First pass: Find exact matches (correct color, correct position) -> black pegs
     * 2. Mark matched positions to avoid counting them twice
     * 3. Second pass: Find color matches in wrong positions -> white pegs
     * 
     * @param guess The player's guess (same length as secretCode)
     * @return int array where [0] = black pegs, [1] = white pegs
     */
    public int[] evaluateGuess(String guess) {
        int blackPegs = 0;
        int whitePegs = 0;
        
        char[] codeArray = secretCode.toCharArray();
        char[] guessArray = guess.toCharArray();
        boolean[] codeMatched = new boolean[codeArray.length];
        boolean[] guessMatched = new boolean[guessArray.length];
        
        // First pass: Find exact matches (black pegs)
        for (int i = 0; i < guessArray.length; i++) {
            if (guessArray[i] == codeArray[i]) {
                blackPegs++;
                codeMatched[i] = true;
                guessMatched[i] = true;
            }
        }
        
        // Second pass: Find color matches in wrong positions (white pegs)
        for (int i = 0; i < guessArray.length; i++) {
            if (!guessMatched[i]) {
                for (int j = 0; j < codeArray.length; j++) {
                    if (!codeMatched[j] && guessArray[i] == codeArray[j]) {
                        whitePegs++;
                        codeMatched[j] = true;
                        break;
                    }
                }
            }
        }
        
        return new int[]{blackPegs, whitePegs};
    }
    
    public String getSecretCode() {
        return secretCode;
    }
}
