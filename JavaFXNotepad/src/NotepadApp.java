//cd C:\Users\tizit\OneDrive\Desktop\3rd2nd\AP\JavaFXNotepad
//javac --module-path ./lib --add-modules javafx.controls,javafx.fxml NotepadApp.java
//java --module-path lib --add-modules javafx.controls,javafx.fxml -cp src NotepadApp


//javac --module-path ../lib --add-modules javafx.controls,javafx.fxml NotepadApp.java
//run
//java --module-path ./lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics "-Djava.library.path=./lib" -cp out NotepadApp

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;

public class NotepadApp extends Application {
    private TextArea textArea = new TextArea();
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // 1. Create the Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);

        // 2. Setup Actions (Logic)
        newItem.setOnAction(e -> textArea.clear());
        openItem.setOnAction(e -> openFile());
        saveItem.setOnAction(e -> saveFile());
        exitItem.setOnAction(e -> primaryStage.close());

        // 3. Layout (Chapter 1)
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(textArea);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("JavaFX Notepad");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Logic for Opening a File (Chapter 2)
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                textArea.setText(content);
            } catch (IOException e) {
                showError("Could not open file.");
            }
        }
    }

    // Logic for Saving a File (Chapter 2)
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), textArea.getText());
            } catch (IOException e) {
                showError("Could not save file.");
            }
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}