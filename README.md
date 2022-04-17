# Wordle-Standalone
A standalone Wordle implementation using JavaFX libraries.
This implementation uses API calls to verify the validity or words and generate a random 5 letter word that is the chosen word for the Wordle. To generate the 5 letter random word, the implementation makes use of a [*random-word-api*](https://random-word-api.herokuapp.com/home) through an HTTPUrlConnection and InputStream. Whether the chosen word is valid, and if the users guesses are valid is verified by the [*dictionary-api*](https://dictionaryapi.dev/). Some valid words that the user enters may be treated as invalid by this API.

## Gameplay and GUI
The main window displays a standard wordle display, including a title (in Courier New), and exit button (and "X" in the corner of the screen), and and a 5 by 6 grid of textfields. The textfields cannot be directly interacted by the user, and only allow input when the preceding textfield has been filled. The behaviour of the textfields, keyboard input from the user, and the GUI are all set in the start(Stage primaryStage) method common in JavaFX applications.

If the user:
1) Presses a character on the keyboard, the character will automatically display in the next empty textfield.
2) Presses the backspace key, the preceding filled textfield will be cleared.
3) Presses the enter key with invalid input, the entire row of textfields will be cleared.
4) Presses the enter key with a valid input:
    - If the word is the chosen word, the entire row of textfields will turn green and the user will no longer be able to input in any more textfields.
    - If the word is not the chosen word, all letters in the right position will turn green, and letters belong to the chosen word but in a wrong position turn yellow.
    - All letters that do not belong to the word are turned grey.

In addition to this, the setRandomWord() method uses the aforementioned API calls to set the chosen word, and the checkInput() method allows the program to check:
1) The validity of the word using the dictionary API.
2) The shared letters and letter positions between the chosen word and guessed word, in order to change the color of the textfields.
3) If the user guesses the correct word, in which case a custom alert is displayed on the screen displaying the # of tries it took to guess the chosen word.
4) If the user completes all 6 tries without guessing the chosen word, in which case the chosen word is displayed to the user.

Finally the onExit() method deals with confirming whether the user wants to exit in the case of the user clicking the "X" button displayed on the screen. This button is needed because the window border and alert border have actually been removed from both the main window and alerts that display in the case of winning, losing, or exiting the game. As well, the button used in the alerts have also been fully customized to match the black and white design of the implementation.

## Examples

![image](https://user-images.githubusercontent.com/55364141/163679558-15efe82b-c947-4d3b-b703-c978128a1d4e.png)
![image](https://user-images.githubusercontent.com/55364141/163679616-f1ba66a6-c1ec-419a-a191-d4625b1e1420.png)
![image](https://user-images.githubusercontent.com/55364141/163679673-895d9cd4-e75f-46f6-b491-3ea2e50b109e.png)
![image](https://user-images.githubusercontent.com/55364141/163679690-1a93e370-8320-43c3-ada1-ecde69b8120a.png)

This implementation was inspired by the Wordle word game owned by The New York Times.
