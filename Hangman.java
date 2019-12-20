import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Hangman extends Application {
    int gamesPlayed = 1;
    boolean trueGuess;
    boolean playingGame = false;
    boolean gameOver = false;
    int guesses = 10;
    BorderPane root = new BorderPane();
    Button start = new Button("Start", new ImageView(new Image("file:./icons/New.png")));
    Button load = new Button("Load", new ImageView(new Image("file:./icons/Load.png")));
    Button save = new Button("Save", new ImageView(new Image("file:./icons/Save.png")));
    Button exit = new Button("Exit", new ImageView(new Image("file:./icons/Exit.png")));
    Button play = new Button("Start Playing");
    HBox toolbar = new HBox();
    BorderPane playingField = new BorderPane(); //inner borderpane where the game is played
    Text hangmanText = new Text("HANGMAN");
    HBox hangmanTitle = new HBox();
    VBox rightPlayingField = new VBox();
    HBox guessesRemaining = new HBox();
    Text initialGuesses = new Text("Remaining guesses: " + guesses);
    GridPane alphabet = new GridPane();
    ArrayList<String> words = new ArrayList<>();
    GridPane hiddenWord = new GridPane();
    ArrayList<String> typedLetters = new ArrayList<>();
    String randomWord = "";
    Stage dialogue = new Stage();
    FileChooser fileChooser = new FileChooser();
    ArrayList<Shape> shapeArrayList = new ArrayList<>(Arrays.asList(new Line(0, 0, -65, 0),
            new Line(-65, 0, -65, 185),
            new Line(-65, 185, 65, 185), new Line(65, 185, 65, 160),
            new Circle(65, 160, 10), new Line(65, 150, 65, 120),
            new Line(65, 135, 75, 145), new Line(65, 135, 55, 145),
            new Line(65, 120, 75, 110), new Line(65, 120, 55, 110)));
    Pane picture = new Pane();
    Group picFragments = new Group();
    HBox picHolder = new HBox();
    int numPic = -1;


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hangman");
        primaryStage.getIcons().add(new Image("file:./icons/Title.png"));
        makeToolbar();
        exit.setOnMouseClicked(e -> System.exit(0)); //if this button is clicked, exits program
        start.setOnMouseClicked(e -> startPressed());
        play.setOnMouseClicked(e -> playPressed());
        root.setOnKeyTyped(e -> makingGuesses(e));
        load.setOnMouseClicked(e -> loadFile());
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {Platform.exit(); System.exit(0);});
    }

    public void makeToolbar(){
        toolbar.setStyle("-fx-background-color: coral;");
        toolbar.setMinHeight(45);
        save.setDisable(true);
        toolbar.setSpacing(7);
        toolbar.getChildren().addAll(start, load, save, exit);
        start.translateYProperty().setValue(8); load.translateYProperty().setValue(8);
        exit.translateYProperty().setValue(8); save.translateYProperty().setValue(8);
        root.setTop(toolbar);
    }

    public void buttonsGameStarted(boolean b){
        if(!b){
            start.setOnMouseClicked(event -> confirmNewGame());
            exit.setOnMouseClicked(exit -> confirmExit());
        } else{
            return;
        }
    }

    public void setUpShapes(){
        picFragments = new Group();
        picture.getChildren().add(picFragments);
        picHolder.getChildren().add(picture);
        picHolder.setRotate(180);
        picHolder.setLayoutX(200);
        picHolder.setLayoutY(300);
        playingField.getChildren().add(picHolder);
    }

    public void addShapeAtFalse(){
        numPic++;
        picFragments.getChildren().add(shapeArrayList.get(numPic));
    }

    public void makingGuesses(KeyEvent e){
        if(play.isDisabled()==false){ //if game has not started do not read keys
            return;
        }
        buttonsGameStarted(gameOver);
        playingGame = true;
        String str = e.getCharacter().toUpperCase();
        char c = str.charAt(0);
        if(Character.isLetter(c)){
            for(int i = 0; i < typedLetters.size(); i++){
                if(str.equals(typedLetters.get(i))){
                    e.consume(); //if letter is already typed, ignore the key press
                    return;
                }
            }
            save.setDisable(false);
            save.setOnMouseClicked(event -> saveFile());
            typedLetters.add(str);
            trueGuess = false;
            int x = 0; int y = 0;
            StackPane temp = null;
            for(int i = 1; i <= 26; i++){
                temp = (StackPane) getNodeFromAlphabet(x, y);
                Text letter = (Text) temp.getChildren().get(1);
                String s = letter.getText();
                if(letter.getText().equals(str)){ //if the letters match, break out of loop
                    break;
                }
                if(i % 7 !=0){
                    y++;
                } else{
                    x++;
                    y = 0;
                }
            }
            Rectangle box = (Rectangle) temp.getChildren().get(0); //change the background of the letterbox
            box.setFill(Color.LIGHTCORAL);
            findHiddenLetter(str);
            if(trueGuess == false){
                guesses--;
                addShapeAtFalse();
            }
            initialGuesses.setText("Remaining guesses: " + guesses);
            if(checkWin()){
                winPopUp();
                e.consume();
            }
            if(guesses == 0){
                showWord();
                losePopUp();
                return;
            }
            playingGame = true;
        } else{
            e.consume();
        }
    }

    public Node getNodeFromAlphabet(int row, int col){
        for(Node node : alphabet.getChildren()){
            if(alphabet.getRowIndex(node) == row && alphabet.getColumnIndex(node) == col){
                return node;
            }
        }
        return null;
    }

    public void colorUsedAlphabet(String letter){
        StackPane sp = null;
        for(Node node : alphabet.getChildren()){
            sp = (StackPane) node;
            Rectangle box = (Rectangle) ((StackPane) node).getChildren().get(0);
            Text text = (Text)((StackPane) node).getChildren().get(1);
            String alphabet = text.getText();
            if(letter.equals(alphabet)){
               box.setFill(Color.LIGHTCORAL);
               return;
           }
        }
    }

    public void losePopUp(){
        save.setDisable(true);
        setButtonsGameOver();
        gameOver = true;
        playingGame = false;
        dialogue.setTitle("Game Over");
        StackPane losePane = new StackPane();
        Text loseMessage = new Text();
        loseMessage.setText("You lost. The word was " + randomWord);
        loseMessage.setStyle("-fx-font: 15 arial;");
        Button close = new Button("CLOSE");
        close.setOnMouseClicked(e -> dialogue.close());
        VBox box = new VBox();
        box.setPadding(new Insets(5));
        box.getChildren().addAll(loseMessage, close);
        box.setAlignment(Pos.CENTER);
        losePane.getChildren().addAll(box);
        dialogue.setScene(new Scene(losePane, 300, 300));
        dialogue.show();
    }

    public void winPopUp(){
        save.setDisable(true);
        setButtonsGameOver();
        gameOver = true;
        dialogue.setTitle("Game Over");
        StackPane winPane = new StackPane();
        Text winMessage = new Text();
        winMessage.setText("You won!");
        winMessage.setStyle("-fx-font: 15 arial;");
        Button close = new Button("CLOSE");
        close.setOnMouseClicked(e -> dialogue.close());
        VBox box = new VBox();
        box.setPadding(new Insets(5));
        box.getChildren().addAll(winMessage, close);
        box.setAlignment(Pos.CENTER);
        winPane.getChildren().addAll(box);
        dialogue.setScene(new Scene(winPane, 300, 300));
        dialogue.show();
        playingGame = false;
    }

    public void setButtonsGameOver(){
        start.setOnMouseClicked(e -> {makeNewGame(); gameOver = false;});
        exit.setOnMouseClicked(e -> {Platform.exit(); System.exit(0);});
    }

    public void confirmExit(){
        dialogue.setTitle("Exit");
        StackPane pane = new StackPane();
        Text warning = new Text();
        warning.setText("Do you want to save?");
        warning.setStyle("-fx-font: 20 arial;");
        Button yes = new Button("Yes");
        yes.setOnMouseClicked(e -> {saveFile(); Platform.exit(); System.exit(0);});
        Button no = new Button("No");
        no.setOnMouseClicked(e -> {Platform.exit(); System.exit(0);});
        Button cancel = new Button("Cancel");
        cancel.setOnMouseClicked(e -> dialogue.close());
        HBox hBox = new HBox();
        hBox.setSpacing(7);
        hBox.getChildren().addAll(yes, no, cancel);
        hBox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(9));
        vbox.getChildren().addAll(warning, hBox);
        vbox.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(vbox);
        dialogue.setScene(new Scene(pane, 400, 400));
        dialogue.show();
    }

    public void confirmNewGame(){
        dialogue.setTitle("New Game?");
        StackPane pane = new StackPane();
        Text warning = new Text();
        warning.setText("Would you like to save the game?");
        warning.setStyle("-fx-font: 20 arial;");
        Button yes = new Button("Yes"); //ask to save game
        yes.setOnMouseClicked(e -> {saveFile(); makeNewGame(); dialogue.close();});
        Button no = new Button("No");
        no.setOnMouseClicked(e -> {makeNewGame();
            dialogue.close();});
        Button cancel = new Button("Cancel");
        cancel.setOnMouseClicked(e -> dialogue.close());
        HBox hBox = new HBox();
        hBox.setSpacing(7);
        hBox.getChildren().addAll(yes, no, cancel);
        hBox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(9));
        vbox.getChildren().addAll(warning, hBox);
        vbox.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(vbox);
        dialogue.setScene(new Scene(pane, 400, 400));
        dialogue.show();
    }

    public void saveFile(){
        String saveLetters = ""; //String will contain all the typed letters(guesses)
        for(String s : typedLetters){
            saveLetters += s;
        }
        String savedLetters = saveLetters;
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName("Hangman Game " + gamesPlayed);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Hng Files (*.hng)", "*.hng"));
        File savedFile = fileChooser.showSaveDialog(null);
        if(savedFile != null){
            try{
                FileOutputStream fileOutputStream = new FileOutputStream(savedFile);
                byte[] str1 = savedLetters.getBytes();
                String newLine = "\n";
                byte[] nl = newLine.getBytes();
                byte[] str2 = randomWord.getBytes();
                fileOutputStream.write(str1);
                fileOutputStream.write(nl);
                fileOutputStream.write(str2);
                fileOutputStream.close();
            } catch(FileNotFoundException ex){
                System.out.println("File dne save file");
            } catch (IOException e) {
                System.out.println("In Writing");
                e.printStackTrace();
            } catch(NullPointerException ex){
                System.out.println("User did not select a file");
            }
        }
    }

    public void loadSavedGameField(String usedLetters, String randomWord){
        play.setDisable(true);
        save.setDisable(true);
        guesses = 10 - usedLetters.length();
        initialGuesses.setText("Remaining guesses: " + guesses);
        initialGuesses.setStyle("-fx-font: 20 arial;");
        playingGame = true;
        typedLetters = new ArrayList<>();
        hangmanTitle = new HBox();
        playingField = new BorderPane();
        hangmanText = new Text("HANGMAN");
        rightPlayingField = new VBox();
        guessesRemaining = new HBox();
        alphabet = new GridPane();
        hiddenWord = new GridPane();
        picture = new Pane();
        picHolder = new HBox();
        numPic = -1;
        for(int i = 0; i < usedLetters.length(); i++){ //set all values of playing field
            typedLetters.add(Character.toString(usedLetters.charAt(i))); // add used characters to list
        }
        setUpShapes();
        hangmanText.setFill(Color.BLACK);
        hangmanText.setStyle("-fx-font: 48 arial;" +
                "-fx-font-weight: bold");
        hangmanTitle.getChildren().add(hangmanText);
        hangmanTitle.setAlignment(Pos.CENTER);
        makeAlphabet();
        setSavedWord(randomWord);
        for(String s : typedLetters){
            findHiddenLetter(s); //show letter in black boxes
            if(trueGuess){
                guesses++;
            } else{
                addShapeAtFalse();
            }
            colorUsedAlphabet(s); //red out letters used
        }
        initialGuesses.setText("Remaining guesses: " + guesses);
        guessesRemaining.getChildren().add(initialGuesses);
        playingField.setTop(hangmanTitle);
        rightPlayingField.getChildren().addAll(guessesRemaining, hiddenWord, alphabet);
        playingField.setRight(rightPlayingField);
        initialGuesses.setX(700); initialGuesses.setY(100);
        root.setCenter(playingField);
    }

    public void loadFile(){
        String usedLetters = "";
        //String randomWord = "";
        fileChooser.setTitle("Open File");
        File game = fileChooser.showOpenDialog(dialogue);
        if(game != null){
            try{
                FileInputStream fis = new FileInputStream(game);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                usedLetters = reader.readLine(); //string of letters used
                randomWord = reader.readLine(); //string of the random word
                reader.close();
                fis.close();
                loadSavedGameField(usedLetters, randomWord); //only if file is read
            } catch(FileNotFoundException ex){
                System.out.println("File dne load file");
            } catch (IOException e) {
                System.out.println("in reading");
                e.printStackTrace();
            } catch(NullPointerException ex){
                System.out.println("User did not select a file");
            }
        }
    }


    public boolean checkWin(){
        StackPane temp = null;
        for(Node child: hiddenWord.getChildren()){
            temp = (StackPane) child;
            Text text = (Text) temp.getChildren().get(1);
            if(text.getFill() == Color.BLACK){
                return false;
            }
        }
        playingGame = false;
        return true;
    }

    public void startPressed(){
            HBox bottomTool = new HBox();
            bottomTool.setAlignment(Pos.BOTTOM_CENTER);
            bottomTool.setStyle("-fx-background-color: white;");
            bottomTool.getChildren().add(play);
            root.setBottom(bottomTool); ;
    }

    public void playPressed(){
            play.setDisable(true);
            setUpShapes();
            hangmanText.setFill(Color.BLACK);
            hangmanText.setStyle("-fx-font: 48 arial;" +
                    "-fx-font-weight: bold");
            hangmanTitle.getChildren().add(hangmanText);
            hangmanTitle.setAlignment(Pos.CENTER);
            initialGuesses.setStyle("-fx-font: 22 arial;");
            makeAlphabet();
            getWords();
            guessesRemaining.getChildren().add(initialGuesses);
            playingField.setTop(hangmanTitle);
            rightPlayingField.getChildren().addAll(guessesRemaining, hiddenWord, alphabet);
            playingField.setRight(rightPlayingField);
            initialGuesses.setX(700); initialGuesses.setY(100);
            root.setCenter(playingField);
    }

    public void findHiddenLetter(String guess){
        StackPane temp = null;
        trueGuess = false;
        for(Node child: hiddenWord.getChildren()){
            temp = (StackPane) child;
            Text text = (Text) temp.getChildren().get(1);
            String letter = text.getText();
            if(guess.equals(letter)){
                text.setFill(Color.WHITE);
                trueGuess = true;
            }
        }
    }

    public void showWord(){
        StackPane temp = null;
        for(Node child : hiddenWord.getChildren()){
            temp = (StackPane) child;
            Rectangle box = (Rectangle)temp.getChildren().get(0);
            Text text = (Text)temp.getChildren().get(1);
            if(text.getFill() == Color.BLACK){
                box.setFill(Color.GRAY);
            }
        }
    }

    public void getWords(){ //reads the words in from file and adds to gui
        File f = new File("words.txt");
        try{
            Scanner reader = new Scanner(f);
            while(reader.hasNextLine()){ //adds words into array list from file
                String word = reader.nextLine();
                words.add(word.toUpperCase());
            }
            reader.close();
        } catch (FileNotFoundException ex){
            System.out.println("file does not exist!");
        }
        int randomIndex = (int)(Math.random()*words.size());
        randomWord = words.get(randomIndex);
        for(int i = 0; i < randomWord.length(); i++){
            StackPane letterBox = new StackPane();
            letterBox.setPadding(new Insets(2));
            Rectangle b = new Rectangle(37, 37);
            b.setFill(Color.BLACK);
            Text w = new Text(Character.toString(randomWord.charAt(i)));
            w.setFill(Color.BLACK);
            w.setStyle("-fx-font: 27 arial;");
            letterBox.getChildren().addAll(b, w);
            hiddenWord.add(letterBox, i, 0);
        }
    }

    public void setSavedWord(String savedWord){
        for(int i = 0; i < savedWord.length(); i++){
            StackPane letterBox = new StackPane();
            letterBox.setPadding(new Insets(2));
            Rectangle b = new Rectangle(37, 37);
            b.setFill(Color.BLACK);
            Text w = new Text(Character.toString(savedWord.charAt(i)));
            w.setFill(Color.BLACK);
            w.setStyle("-fx-font: 27 arial;");
            letterBox.getChildren().addAll(b, w);
            hiddenWord.add(letterBox, i, 0);
        }
    }


    public void makeAlphabet(){
        char c = 'A';
        int x = 0; //row
        int y = 0; //column
        for(int i = 1; i <= 26; i++){
            StackPane letterBox = new StackPane();
            letterBox.setPadding(new Insets(3));
            Rectangle box = new Rectangle(45, 45);
            box.setFill(Color.BLUE);
            Text temp = new Text(Character.toString(c));
            temp.setFill(Color.WHITE);
            temp.setStyle("-fx-font: 30 arial;");
            letterBox.getChildren().addAll(box, temp);
            alphabet.add(letterBox, y, x);
            c++;
            if(i%7 != 0){
                y++;
            } else{
                x++;
                y = 0;
            }
        }
    }

    public void makeNewGame(){
        numPic = -1;
        gamesPlayed++;
        guesses = 10;
        initialGuesses.setText("Remaining guesses: " + guesses);
        playingGame = false;
        //reset all values of the playing field
        typedLetters = new ArrayList<>(); //reset types letters list
        words = new ArrayList<>(); //reset words list
        hangmanTitle = new HBox();
        playingField = new BorderPane(); //inner borderpane where the game is played
        hangmanText = new Text("HANGMAN");
        rightPlayingField = new VBox();
        guessesRemaining = new HBox();
        alphabet = new GridPane();
        hiddenWord = new GridPane();
        picHolder = new HBox();
        picture = new Pane();
        playPressed();
    }

    public static void main (String[] args){
        Application.launch(args);
    }
}
