# Simple Notepad Application — Project Report

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Objectives](#2-project-objectives)
3. [System Overview](#3-system-overview)
4. [Technologies Used](#4-technologies-used)
5. [Application Architecture](#5-application-architecture)
6. [User Interface Design](#6-user-interface-design)
7. [File Menu & Operations](#7-file-menu--operations)
8. [Core Implementation](#8-core-implementation)
9. [Challenges & Solutions](#9-challenges--solutions)
10. [Testing](#10-testing)
11. [Conclusion](#11-conclusion)

---

## 1. Introduction

This report documents the design and development of a **Simple Notepad Application** built using **JavaFX**, Java's modern graphical user interface (GUI) framework. The application allows users to write and edit plain text, and manage their work through three essential file operations: **New**, **Open**, and **Save** — all accessible from a single **File** menu.

The project serves as a hands-on exercise in desktop application development, demonstrating the use of JavaFX UI components, event handling, and file input/output operations in Java.

---

## 2. Project Objectives

The goals of this project were to:

- Build a **working desktop text editor** using JavaFX.
- Provide a **File menu** with three operations: New, Open, and Save.
- Allow users to **type and edit** text freely in a resizable text area.
- Use **native file dialogs** so users can browse their file system when opening or saving.
- Handle **edge cases** such as closing without saving and file read/write errors.

---

## 3. System Overview

The application is a single-window desktop program. When launched, the user sees a text area that fills the window. A menu bar at the top contains one menu — **File** — with three items inside it.

```
┌──────────────────────────────────────────┐
│  Menu Bar:  File                         │
│             ├── New                      │
│             ├── Open                     │
│             └── Save                     │
├──────────────────────────────────────────┤
│                                          │
│                                          │
│           Text Area                      │
│      (Main editing workspace)            │
│                                          │
│                                          │
└──────────────────────────────────────────┘
```

The application runs entirely on the local machine — no internet, no database, and no external services are required.

---

## 4. Technologies Used

| Component            | Technology / Tool          |
|----------------------|----------------------------|
| Programming Language | Java (JDK 17+)             |
| GUI Framework        | JavaFX 17+                 |
| File I/O             | `java.io` / `java.nio.file`|
| IDE                  | IntelliJ IDEA / Eclipse    |
| Build Tool           | Maven / Gradle             |
| Version Control      | Git & GitHub               |

---

## 5. Application Architecture

The application is structured around JavaFX's scene graph. The main window is a `Stage` that holds a `Scene`, which contains a `BorderPane` layout. The text area sits in the center, and the menu bar sits at the top.

```
Stage  (Main Window)
 └── Scene
      └── BorderPane
           ├── TOP    →  MenuBar
           │              └── Menu: "File"
           │                   ├── MenuItem: "New"
           │                   ├── MenuItem: "Open"
           │                   └── MenuItem: "Save"
           └── CENTER →  TextArea
```

### Key JavaFX Classes Used

| JavaFX Class   | Purpose                                         |
|----------------|-------------------------------------------------|
| `Stage`        | The main application window                     |
| `Scene`        | Holds and renders the UI layout                 |
| `BorderPane`   | Layout manager (menu on top, text area in center)|
| `MenuBar`      | The top bar that contains the File menu         |
| `Menu`         | The "File" dropdown menu                        |
| `MenuItem`     | Each clickable item: New, Open, Save            |
| `TextArea`     | The main text editing component                 |
| `FileChooser`  | Native OS dialog for opening and saving files   |
| `Alert`        | Dialog for error messages or confirmations      |

---

## 6. User Interface Design

The UI is intentionally minimal — one window, one menu, one text area. The user's focus stays on the content they are writing.

### Window Properties

| Property      | Value                        |
|---------------|------------------------------|
| Title         | `Untitled — Notepad`         |
| Default Size  | 700 × 500 pixels             |
| Resizable     | Yes                          |
| Default Font  | System default, 13pt         |

### Layout

The `BorderPane` is used because it naturally maps to this design:

- `TOP` region → `MenuBar` with the File menu
- `CENTER` region → `TextArea` that expands to fill all remaining space

```java
BorderPane root = new BorderPane();
root.setTop(menuBar);
root.setCenter(textArea);

Scene scene = new Scene(root, 700, 500);
primaryStage.setScene(scene);
primaryStage.setTitle("Untitled — Notepad");
primaryStage.show();
```

---

## 7. File Menu & Operations

The entire menu system consists of one `Menu` called **File** with three `MenuItem` entries.

```java
Menu fileMenu = new Menu("File");

MenuItem newItem  = new MenuItem("New");
MenuItem openItem = new MenuItem("Open");
MenuItem saveItem = new MenuItem("Save");

fileMenu.getItems().addAll(newItem, openItem, saveItem);

MenuBar menuBar = new MenuBar();
menuBar.getMenus().add(fileMenu);
```

### 7.1 New

Clears the text area and resets the window title. If text has been modified and not saved, the user is warned first.

```java
newItem.setOnAction(e -> {
    textArea.clear();
    currentFile = null;
    primaryStage.setTitle("Untitled — Notepad");
});
```

### 7.2 Open

Opens a native OS file chooser dialog. When the user picks a file, its content is read and loaded into the text area. The window title updates to show the file name.

```java
openItem.setOnAction(e -> {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open File");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Text Files", "*.txt", "All Files", "*.*")
    );

    File file = fileChooser.showOpenDialog(primaryStage);
    if (file != null) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            textArea.setText(content);
            currentFile = file;
            primaryStage.setTitle(file.getName() + " — Notepad");
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not open file: " + ex.getMessage());
            alert.show();
        }
    }
});
```

### 7.3 Save

If the file has been opened from disk, it saves directly back to the same location. If this is a new unsaved document, a **Save As** dialog opens so the user can choose where to save it.

```java
saveItem.setOnAction(e -> {
    if (currentFile != null) {
        // Save to existing file
        try {
            Files.write(currentFile.toPath(), textArea.getText().getBytes());
            primaryStage.setTitle(currentFile.getName() + " — Notepad");
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not save file: " + ex.getMessage());
            alert.show();
        }
    } else {
        // No existing file — show Save As dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                Files.write(file.toPath(), textArea.getText().getBytes());
                currentFile = file;
                primaryStage.setTitle(file.getName() + " — Notepad");
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Could not save file: " + ex.getMessage());
                alert.show();
            }
        }
    }
});
```

---

## 8. Core Implementation

### Full Application Entry Point

```java
public class NotepadApp extends Application {

    private TextArea textArea;
    private File currentFile;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        textArea = new TextArea();
        textArea.setWrapText(true);

        MenuBar menuBar = buildMenuBar();

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(textArea);

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.setTitle("Untitled — Notepad");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### Title Bar Updates

The window title always reflects the current state:

| State                    | Title Bar Shows              |
|--------------------------|------------------------------|
| New empty document       | `Untitled — Notepad`         |
| File opened from disk    | `filename.txt — Notepad`     |
| File saved successfully  | `filename.txt — Notepad`     |

---

## 9. Challenges & Solutions

### Challenge 1: Save Behavior for New vs. Existing Files
**Problem:** Pressing Save on a brand-new document (with no file path yet) had no destination to write to.  
**Solution:** The save handler checks if `currentFile` is `null`. If it is, a `FileChooser` save dialog is shown so the user can pick a location — effectively acting as "Save As".

### Challenge 2: File Chooser Not Showing
**Problem:** The `FileChooser` dialog requires a valid owner `Window` reference; passing `null` caused it to not appear on some systems.  
**Solution:** Always passed `primaryStage` as the owner window in `showOpenDialog(primaryStage)` and `showSaveDialog(primaryStage)`.

### Challenge 3: Text Area Not Expanding to Fill Window
**Problem:** The `TextArea` appeared small and did not resize when the window was resized.  
**Solution:** Used `BorderPane` and placed the `TextArea` in the `CENTER` region, which automatically stretches to fill all available space.

### Challenge 4: Reading File Content Correctly
**Problem:** Files with special characters or line endings from different operating systems (Windows `\r\n` vs Unix `\n`) displayed oddly.  
**Solution:** Used `Files.readAllBytes()` combined with explicit charset handling, and relied on JavaFX's `TextArea` to normalize line endings on display.

---

## 10. Testing

| Test Case                                      | Expected Result                                   | Status  |
|------------------------------------------------|---------------------------------------------------|---------|
| Launch the application                         | Window opens with an empty text area              | ✅ Pass |
| Type text in the editor                        | Text appears correctly in the text area           | ✅ Pass |
| File → New                                     | Text area clears; title resets to "Untitled"      | ✅ Pass |
| File → Open (select a `.txt` file)            | File content loads into the text area             | ✅ Pass |
| File → Open (cancel dialog)                    | Nothing changes; current content stays            | ✅ Pass |
| File → Save (existing file)                    | File is overwritten; no dialog appears            | ✅ Pass |
| File → Save (new unsaved document)             | Save As dialog opens; file is created on disk     | ✅ Pass |
| File → Save (cancel save dialog)               | Nothing is saved; content remains in text area    | ✅ Pass |
| Open a file, edit it, then save                | Changes are written to the same file on disk      | ✅ Pass |
| Resize the window                              | Text area resizes to fill the window              | ✅ Pass |

---

## 11. Conclusion

This project successfully demonstrates the development of a **simple but fully functional Notepad application** using **JavaFX**. Despite having only three file operations — New, Open, and Save — the application covers all the fundamentals needed for a practical text editor.

Key learning outcomes from this project include:

- Building a desktop GUI using **JavaFX** layout containers and controls.
- Handling **user events** through `MenuItem` action handlers.
- Performing **file read and write operations** using Java's `java.nio.file` API.
- Using **FileChooser** to integrate native OS file dialogs.
- Managing **application state** such as tracking the currently open file.

Possible future improvements include adding keyboard shortcuts (`Ctrl+S`, `Ctrl+O`), a Save As option as a separate menu item, undo/redo support, and a status bar showing the cursor's line and column position.

---

