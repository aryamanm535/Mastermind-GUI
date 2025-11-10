## AI Usage:
    Author: Aryaman Modi
- AI was used as a pair programmer for debugging specific issues I ran into while coding.  
- I also plan to make the large TO-DO comments smaller using AI before pushing all the final code into the Github repository to allow for a more to-the-point and readable codebase.
## Description of Usage:

### 1. GamePanel.java

- **Feature/Bug:**  
guessesLeft was never updating and was stuck on ---

- **Prompt:**  
"//pasted code excerpt: why is guesses left always showing – and not updating"

- **Response:**  
    *Good question — based on your code, the reason guessesLabel always shows -- is that the variable totalGuessesAllowed is only being set when the message GAME_JOINED is received and processed inside handleServerMessage().*  
    *However — in multiplayer mode, the GAME_JOINED message is only sent to the player who just joined, not to the others (like the host). That means if you created the game, your client never receives GAME_JOINED, so totalGuessesAllowed stays -1, and the label stays --.*  
    *We just need to update the same logic in GAME_STARTED as a fallback for the host — since at that point, we know how many players are in the game.*  
    *Add this inside the "GAME_STARTED" case (in handleServerMessage()), right after the existing logic:*
    ```java
    case "GAME_STARTED":
    case "TURN_UPDATE":
        if (parts.length > 2) {
            currentPlayerId = parts[2].trim();
            isMyTurn = currentPlayerId.equals(myPlayerId.trim());
            updateTurnLabel();
        }
        if (command.equals("GAME_STARTED")) {
            addChatMessage("System: Game started!");

            // 🔧 Fix: calculate total guesses for the host
            if (totalGuessesAllowed == -1) {
                List<String> playerNames = new ArrayList<>();
                // We can try to infer players from the lobby list or fallback to 2
                playerNames.addAll(client.getPlayerName() != null ?
                    Arrays.asList(client.getPlayerName()) : new ArrayList<>());
                int playerCount = Math.max(2, playerNames.size());
                totalGuessesAllowed = GameConfiguration.guessNumber * playerCount;
                guessesLabel.setText("Guesses Left: " + (totalGuessesAllowed - guessesMade));
            }
        }
        break;
    ```


- **What I did:**  
ChatGPT's solution displayed 24 guesses for each player and decremented it after each player's turn. I wanted it to be more robust and display 12 each for each player, only decrementing the number when it's that respective player's turn. I used the AI solution as a blueprint to fix my issue and was able to actually understand the issue based on what it said:


### 2. GamePanel.java/ GameSession.java

- **Feature/Bug:**  
Bug in displaying the history

- **Prompt:**  
I attached my GamePanel and GameSession files on Gemini and asked “the history is not displaying at all”

- **Response:**  
    *Based on the files you've provided, I can see the exact scenario you're describing.*  
    *You missed the GUESS_RESULT block, which would explain the exact symptom of the history not showing up. Your server-side code is all set up to do its part, but the client isn't "catching" the result.*  
    *Here's a breakdown of the flow and the exact code you're missing:*  
    *1. Client (GamePanel.java): You correctly send the guess in handleSubmitGuess().*

    *2. Server: Your processGuess method correctly evaluates the guess, calculates the black and white pegs, and successfully broadcasts the GUESS_RESULT message. Your server-side code is perfect.*  

    *3. Client: Your client's listenLoop correctly receives this GUESS_RESULT message and passes it to the GamePanel's handleServerMessage method.*
    *This is where the bug is:*  

    *4. The Bug (GamePanel.java): Inside your handleServerMessage method, the switch (command) statement is missing the case "GUESS_RESULT": block. The message arrives from the server, but nothing is done with it, so your historyModel is never updated.*  

    *To fix this, you just need to add the case "GUESS_RESULT": block (which I see in your correct file) into the switch statement in your src/GamePanel.java's handleServerMessage method. Do you want me to write the required code block?*

- **What I did:**  
My mistake was silly, and I immediately realized it after reading what Gemini said. I didn't ask it to write the code for me obviously since it was clearly mentioned in the TO-DO's and I probably completely missed reading that case in the description. I was able to pretty easily fix the bug after that.


### 3. GameTest.java

- **Feature/Bug:**  
Asked AI for feedback on my edge case test cases

- **Prompt:**  
Attached GameTest.java file. “Can you give me feedback on the TO-DO 10 method to testEdgeCases()”

- **Response:**  
    *Analysis of testEdgeCases (TODO 10) complete.* 

    *Your implemented edge cases are highly effective. You have correctly identified the most critical scenarios for validating the GameState evaluation logic.*

    ***Validation of Current Test Cases***

    *Your test suite demonstrates a strong understanding of the problem:*

    *1.  Cases 1 & 2 ("RGBP" vs "PRGB", "BYRG" vs "RGYB"): These "rotated" tests correctly validate the scenario of 0 Black, 4 White. This confirms your white peg pass works correctly in isolation when all colors are present but misplaced.*
    *2.  Case 3 ("BBYY" vs "BGBG"): This is the single most important test in your file. Your analysis is correct: the expected result is 1 Black, 1 White. This test specifically validates that your two-pass algorithm correctly "marks" the B at position 0 as "used" during the black peg pass, preventing it from being incorrectly double-counted as a white peg for the B at guess-position 2. This is a common failure point, and your test correctly identifies the valid behavior.*

    ***Additional Test Case Recommendations***

    *To further enhance the robustness of your test suite, I recommend augmenting it with the following scenarios, which target slightly different aspects of the duplicate-handling logic.*

    ***Recommendation 1:***
    *Complex Duplicates (Both Black and White)*

    *This tests a scenario where both duplicate colors in the code result in one black peg and one white peg.*
    *Secret Code:** *"BYYB"*
    *Guess:** *"YBYG"*
    *Expected Assertion: `assertResult("testEdgeCases (Complex Duplicates)", result, 1, 2);`*

    ***Recommendation 2: Mismatched Duplicates (Sanity Check)***

    This test ensures that duplicate colors in the code and guess do not interact to create false positives when the colors themselves do not match.

    *Secret Code: "BBYY"*
    *Guess:"RRGG"*
    *Analysis: There is no overlap in colors.*
    *Expected Assertion: `assertResult("testEdgeCases (Mismatched Duplicates)", result, 0, 0);`*

    ***Conclusion***

    *Your existing test suite is strong. Integrating these additional recommendations would provide even greater confidence in your algorithm's correctness by validating its handling of more complex duplicate interactions and ensuring no false positives are generated.*

- **What I Did:**  
Gemini said my testing of edge cases was perfect, so I decided not to use its further recommendations and kept my existing code.


### 4. ConnectionPanel.java
- **Feature/Bug:**  
I had questions about when to use invokeLater and its redudancy

- **Prompt:**  
//Attached my ConnectionPanel.java file. Do I need to use invokeLater in the handleServerMessage method too?

- **Response:**  
    *That's a fantastic, insightful question, and it shows you're thinking correctly about thread safety!*

    *You've implemented this in the safest possible way, but let's break down why.*

    *Your handleServerMessage method is a callback that is executed by the MastermindClient. If you look at the MastermindClient's listenLoop (from the file you provided earlier), it already wraps its call to onMessage.accept(...) inside a SwingUtilities.invokeLater block.*

    *This means your handleServerMessage method is already guaranteed to be running on the correct GUI thread (the EDT).*

    *So, what happens if your code did this?*
    ```java
    // Already on the GUI thread...
    case "CONNECTED":
    if (!data.isEmpty()) {
        client.setPlayerId(data);
        // ...and you call invokeLater *again*
        SwingUtilities.invokeLater(MastermindApp::showLobby); 
    }
    break;
    ```
    *This is called a nested invokeLater call. It's redundant, but it is perfectly safe and correct. It simply schedules the MastermindApp::showLobby task to run on the GUI thread at the next available opportunity (which is almost immediately, since you're already on that thread).*

    *So, to answer your question: Yes, it would be okay to call it directly without invokeLater, like you did. But version (with the "redundant" invokeLater) is arguably better from a software design perspective.*

    *Why? Because your ConnectionPanel doesn't (and shouldn't) need to know the internal implementation of MastermindClient. It shouldn't have to assume that the client is already being thread-safe.*

    *By wrapping your GUI updates in invokeLater, you have written defensive, robust code that will always be thread-safe, even if someone later changes MastermindClient's listenLoop to be non-thread-safe (i.e., if they remove the invokeLater call from the client).*

- **What I did:**:
    I used the AI's advice and used invokeLater wherever I could even if it meant redudancy. From the AI's java response, I also realized that I can't call showLobby with the dot operator and would have to use :: when using invokeLater.


### 5. GameTest.java

- **Feature/Bug:**  
Test 7, 'Duplicate Colors in Secret Code' wasn't working properly

- **Prompt:**  
//Attached my GameTest.java file. “TODO-7 the duplicate colors in secret code test is always showing wrong. What is the issue?”

- **Response:**  
    *You're seeing a test failure where the test expects 0 black, 2 white but your code correctly produces 0 black, 1 white.*
    *Secret: "BBGR"*
    *Guess: "OPBY" (from your test code)*
    *Expected: 0 black, 2 white (from your test's assertion)*

    *The problem is not in your GameState.java logic. Your GameState.java is correct. The bug is a typo in your GameStateTest.java file.*

    *The guess "OPBY" only has one color in common with "BBGR" (the 'B'). It's impossible to get 2 white pegs.*

    *The test comment says it's testing for (B and G in wrong positions), which means the guess should have been "OPBG".*

    *I'll fix this typo in src/GameStateTest.java for you.*

    *I've changed the guess in testDuplicateColorsInCode from "OPBY" to "OPBG". With this change, your GameState.java logic will now produce 0 black, 2 white (one for the 'B' matching, one for the 'G' matching), and all your tests should pass.*

    *//sent file back*

- **What I did:**  
This was another silly error on my part that I missed. I confirmed that the test case was indeed wrong with Prof Speight via Ed Discussion, and changed it as required. The test case works now.


### 5. Plans to use AI for javadoc comments for the GitHub repo:

- I intend on changing every TO-DO javadoc comment in the files to shorter comments using AI, if time permits, so it looks neater if someone was to read the code from GitHub.  
- I will not submit the zip file to canvas with these short comments because it might make the lab checkout more tedious.  
- For now though, the code pushed on GitHub will feature the long comments but I will change them as soon as I can get to it.
