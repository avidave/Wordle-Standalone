package com.avi;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.StageStyle;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.stream.Collectors;

public class WordleMain extends Application {

    private static final TextField[][] fields = new TextField[6][5];
    private static int emptyFieldRow = 0;
    private static int emptyFieldColumn = 0;
    private static String wordOfTheDay = "";
    private static String checkWord;
    private boolean gotTheWord = false;
    private static Button exitBtn;
    private int turnsLeft = 6;

    /**
     * Start sets up the entire GUI, and sets the code for the keyboard input of the textfields.
     * This implements the abstract method 'start' of JavaFX application
     * @param primaryStage: is automatically called for JavaFx applications.
     */
    public void start(Stage primaryStage) {

        try {

            // choose a random 5 letter word for the Wordle
            setRandomWord();

            Pane root = new Pane();
            Scene scene = new Scene(root,800,1000);
            root.setStyle("-fx-background-color: black");

            Font titleFont = Font.font("Courier New", 55);

            Label titleLabel = new Label();
            titleLabel.setText("WORDLE");
            titleLabel.setTextAlignment(TextAlignment.CENTER);
            titleLabel.setFont(titleFont);
            titleLabel.setPrefSize(scene.getWidth(), 70);
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setAlignment(Pos.CENTER);
            titleLabel.setLayoutY(20);

            int X;
            int Y = 100;

            for (int r = 0; r < fields.length; r++) {

                X = 150;

                for (int c = 0; c < fields[r].length; c++) {

                    fields[r][c] = new TextField();
                    fields[r][c].setStyle("-fx-control-inner-background: black");
                    fields[r][c].setLayoutX(X);
                    fields[r][c].setLayoutY(Y);
                    fields[r][c].setPrefSize(90, 90);
                    fields[r][c].setFont(Font.font("Courier New", 40));
                    fields[r][c].setAlignment(Pos.CENTER);
                    fields[r][c].setEditable(false);
                    fields[r][c].setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));

                    fields[0][0].setBorder(new Border(new BorderStroke(Color.ORANGERED, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                    fields[0][0].deselect();

                    fields[r][c].setOnKeyPressed(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent e) {

                            TextField nextField;
                            TextField lastField;

                            if (emptyFieldColumn == 5) {
                                nextField = fields[emptyFieldRow][emptyFieldColumn - 1];
                            } else if (emptyFieldRow == 6) {
                                nextField = fields[emptyFieldRow - 1][emptyFieldColumn];
                            } else {
                                nextField = fields[emptyFieldRow][emptyFieldColumn];
                            }
                            nextField.setBorder(new Border(new BorderStroke(Color.ORANGERED, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));

                            if (emptyFieldColumn >= 1) {
                                lastField = fields[emptyFieldRow][emptyFieldColumn - 1];
                                lastField.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                            } else {
                                lastField = null;
                            }

                            if (e.getText().matches("[A-Za-z]")) {

                                if (!gotTheWord) {

                                    if (emptyFieldRow <= 6) {
                                        nextField.setText(e.getText().toUpperCase());

                                        if (emptyFieldColumn < 5 && emptyFieldRow < 6) {
                                            emptyFieldColumn++;
                                        }
                                    }
                                }

                            } else if (e.getCode() == KeyCode.ENTER) {

                                for (int i = 0; i < fields[emptyFieldRow].length; i++) {
                                    if (fields[emptyFieldRow][i].getText().isEmpty()) {
                                        break;
                                    } else if (i == 4) {
                                        try {
                                            checkInput(fields[emptyFieldRow][0].getText() + fields[emptyFieldRow][1].getText() + fields[emptyFieldRow][2].getText()
                                            + fields[emptyFieldRow][3].getText() + fields[emptyFieldRow][4].getText());
                                        } catch (Exception event) {
                                            event.printStackTrace();
                                        }
                                    }
                                }

                            } else if (e.getCode() == KeyCode.BACK_SPACE) {

                                nextField.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));

                                if (emptyFieldColumn > 0) {
                                    emptyFieldColumn--;
                                    fields[emptyFieldRow][emptyFieldColumn].setBorder(new Border(new BorderStroke(Color.ORANGERED, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                                }

                                if (lastField == null) {
                                    nextField.clear();
                                } else {
                                    lastField.clear();
                                }
                            }
                        }
                    });

                    X = X + 105;
                }

                Y = Y + 105;
            }

            for (TextField[] t: fields) {
                for (TextField f: t) {
                    root.getChildren().add(f);
                }
            }

            exitBtn  = new Button();
            exitBtn.setText("X");
            exitBtn.setFont(Font.font("Arial", 45));
            exitBtn.setAlignment(Pos.CENTER);
            exitBtn.setLayoutX(scene.getWidth() - 110);
            exitBtn.setLayoutY(12.5);
            exitBtn.setBackground(Background.EMPTY);
            exitBtn.setTextFill(Color.WHITE);
            exitBtn.setOnAction(e -> onExit());

            root.getChildren().addAll(titleLabel, exitBtn);

            root.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(4), null)));

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setTitle("WORDLE");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method sets the random word by using api calls.
     * It first gets a random word from the random-word-api and check if it is valid.
     * It then checks whether the word is valid using the dictionary-api.
     * Finally, it makes sure that the word chosen has not been used previously by reading from a text file and if it is not, the word is written into the file for the next run
     * of the program.
     * @throws IOException
     */
    private void setRandomWord() throws IOException {
        URL url, checkUrl;
        HttpURLConnection connection, secConnection;
        InputStream responseStream, checkStream;
        BufferedReader in = new BufferedReader(new FileReader(new File("src/main/resources/usedWords.txt")));
        String usedWords = in.lines().collect(Collectors.joining());
        while (true) {

            try {

                url = new URL("https://random-word-api.herokuapp.com/word?length=5");

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("accept", "application/json");

                responseStream = connection.getInputStream();

                wordOfTheDay = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")).substring(2, 7);

                if (usedWords.contains(wordOfTheDay)) {
                    continue;
                }

                checkUrl = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + wordOfTheDay);
                secConnection = (HttpURLConnection) checkUrl.openConnection();

                secConnection.setRequestProperty("accept", "application/json");

                checkStream = secConnection.getInputStream();

                checkWord = new BufferedReader(new InputStreamReader(checkStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                String w = checkWord.split("\"")[3];

                BufferedWriter out = new BufferedWriter(new FileWriter(new File("src/main/resources/usedWords.txt"), true));
                out.write(wordOfTheDay + "\n");
                out.close();
                break;


            } catch (Exception e) {

            }
        }
    }

    /**
     * This method uses an api call to check the validity of a word. If the word is valid, it does not clear the filled textfields in the GUI, and instead checks if
     * each letter is:
     * 1) In the chosen word (turns yellow; if not turns grey).
     * 2) Is in the right place in the word (turns green).
     * This method also decides whether the user may continue to output words. An alert is displayed whether the user has guessed the chosen word, or if the user runs out
     * of tries.
     * @throws IOException
     * @param word: The string inputted into a row of textfields after the user presses enter. The methdo checks if the word is valid.
     */
    private void checkInput(String word) throws IOException {

        Alert endAlert = new Alert(Alert.AlertType.NONE);
        endAlert.setContentText(null);

        Label textLabel = new Label();
        textLabel.setTextFill(Color.WHITE);
        textLabel.setMinSize(400, 100);
        textLabel.setFont(Font.font("Courier New", 20));
        textLabel.setAlignment(Pos.CENTER);

        Button yesButton = new Button();
        yesButton.setText("CLOSE");
        yesButton.setFont(Font.font("Courier New", 20));
        yesButton.setTextFill(Color.WHITE);
        yesButton.setBackground(Background.EMPTY);
        yesButton.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(20), null)));
        yesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage) endAlert.getDialogPane().getScene().getWindow();
                stage.close();

            }
        });
        yesButton.setLayoutY(100);
        yesButton.setLayoutX(310);

        Pane endPane = new Pane();
        endPane.getChildren().addAll(textLabel, yesButton);

        endAlert.getDialogPane().setContent(endPane);
        endAlert.getDialogPane().setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(10), null)));
        endAlert.getButtonTypes().clear();
        endAlert.getDialogPane().setStyle("-fx-background-color: black");
        endAlert.initStyle(StageStyle.UNDECORATED);
        endAlert.getButtonTypes().clear();
        endAlert.setHeaderText(null);
        endAlert.getDialogPane().setMinSize(400, 100);

        System.out.print(word);

        URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word.toLowerCase());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("accept", "application/json");

        try {
            InputStream responseStream = connection.getInputStream();

            checkWord = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            String w = checkWord.split("\"")[3];

            if (w.equals(wordOfTheDay)) {
                for (TextField field : fields[emptyFieldRow]) {
                    field.setStyle("-fx-control-inner-background: green");
                    field.setBorder(new Border(new BorderStroke(Color.DARKGREEN, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                }
                gotTheWord = true;

                if (emptyFieldRow == 0) {
                    textLabel.setText("Wow! Got the word in " + (emptyFieldRow + 1) + " try!");
                } else {
                    textLabel.setText("Wow! Got the word in " + (emptyFieldRow + 1) + " tries!");
                }

                endAlert.showAndWait();

            } else {

                turnsLeft--;

                for (int l = 0; l < w.length(); l++) {
                    char letter = w.charAt(l);
                    if (wordOfTheDay.contains(Character.toString(letter))) {
                        if (wordOfTheDay.charAt(l) == letter) {
                            fields[emptyFieldRow][l].setStyle("-fx-control-inner-background: green");
                            fields[emptyFieldRow][l].setBorder(new Border(new BorderStroke(Color.DARKGREEN, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                        } else {
                            fields[emptyFieldRow][l].setStyle("-fx-control-inner-background: yellow");
                            fields[emptyFieldRow][l].setBorder(new Border(new BorderStroke(Color.rgb(246,190,0), BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                        }
                    } else {
                        fields[emptyFieldRow][l].setStyle("-fx-control-inner-background: #3a3a3c");
                        fields[emptyFieldRow][l].setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM)));
                    }
                }

                if (emptyFieldRow < 5) {
                    emptyFieldRow++;
                    emptyFieldColumn = 0;
                }

                if (turnsLeft == 0) {
                    textLabel.setText("The word is "+wordOfTheDay+".\nBetter luck next time!!");
                    endAlert.showAndWait();
                }
            }
        } catch (FileNotFoundException fnf) {
            for (TextField field : fields[emptyFieldRow]) field.setText("");
            emptyFieldColumn = 0;
        }
    }

    /**
     * When the user presses the exit button "X", an alert is displayed confirming whether the user really wishes to exit.
     */
    private void onExit() {

        Alert exitAlert = new Alert(Alert.AlertType.NONE);
        exitAlert.setContentText(null);

        Label textLabel = new Label();
        textLabel.setText("Thank you for playing!!");
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setTextFill(Color.WHITE);
        textLabel.setMinSize(400, 100);
        textLabel.setFont(Font.font("Courier New", 20));

        Button yesButton = new Button();
        yesButton.setText("EXIT");
        yesButton.setFont(Font.font("Courier New", 20));
        yesButton.setTextFill(Color.WHITE);
        yesButton.setBackground(Background.EMPTY);
        yesButton.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(20), null)));
        yesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });
        yesButton.setLayoutY(100);
        yesButton.setLayoutX(310);

        Button noButton = new Button();
        noButton.setText("X");
        noButton.setFont(Font.font("Arial", 30));
        noButton.setTextFill(Color.WHITE);
        noButton.setBackground(Background.EMPTY);
        noButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = (Stage) exitAlert.getDialogPane().getScene().getWindow();
                stage.close();
            }
        });
        noButton.setLayoutY(20);
        noButton.setLayoutX(365);
        Pane endPane = new Pane();
        endPane.getChildren().addAll(textLabel, yesButton, noButton);

        exitAlert.getDialogPane().setContent(endPane);
        exitAlert.getDialogPane().setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(10), null)));
        exitAlert.getButtonTypes().clear();
        exitAlert.getDialogPane().setStyle("-fx-background-color: black");
        exitAlert.initStyle(StageStyle.UNDECORATED);
        exitAlert.getButtonTypes().clear();

        exitAlert.setHeaderText(null);
        exitAlert.showAndWait();
    }
}
