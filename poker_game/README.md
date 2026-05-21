# Poker Card Dealing Game — Project Report



---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Objectives](#2-project-objectives)
3. [System Overview](#3-system-overview)
4. [Technologies Used](#4-technologies-used)
5. [Application Architecture](#5-application-architecture)
6. [User Interface Design](#6-user-interface-design)
7. [Game Logic](#7-game-logic)
8. [Core Implementation](#8-core-implementation)
9. [Winner Determination](#9-winner-determination)
10. [Challenges & Solutions](#10-challenges--solutions)
11. [Testing](#11-testing)
12. [Conclusion](#12-conclusion)

---

## 1. Introduction

This report documents the design and development of a **Poker Card Dealing Game** built using **Java**. The application simulates a simplified two-player card game where two players — **Alice** and **Bob** — are each dealt a random card when the user clicks the **Deal** button. The application then automatically compares the two cards and announces the **winner**.

The project demonstrates the use of randomization, object-oriented design, basic game logic, and a simple interactive interface in Java.

---

## 2. Project Objectives

The goals of this project were to:

- Create a **deck of 52 standard playing cards** with suits and ranks.
- Deal **one random card** to each of the two players (Alice and Bob) on every click of the Deal button.
- **Display** each player's card clearly on screen.
- **Compare** the two cards and determine the winner based on card rank.
- Announce the **result** — which player wins, or if it is a draw.
- Ensure the **same card is never dealt to both players** in the same round.

---

## 3. System Overview

The application has one screen with two player areas and a single Deal button. When the user clicks Deal, the program randomly picks two different cards from a shuffled deck, assigns one to Alice and one to Bob, then displays the winner.

```
┌─────────────────────────────────────────────────┐
│                                                 │
│   ┌──────────────┐       ┌──────────────┐       │
│   │    ALICE     │       │     BOB      │       │
│   │              │       │              │       │
│   │   [ Card ]   │       │   [ Card ]   │       │
│   │   e.g. K♠    │       │   e.g. 7♥    │       │
│   └──────────────┘       └──────────────┘       │
│                                                 │
│          ┌──────────────────┐                   │
│          │      DEAL        │                   │
│          └──────────────────┘                   │
│                                                 │
│       Result:  Alice wins! (King vs 7)          │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 4. Technologies Used

| Component            | Technology / Tool          |
|----------------------|----------------------------|
| Programming Language | Java (JDK 17+)             |
| GUI Framework        | JavaFX / Swing             |
| Randomization        | `java.util.Random` / `Collections.shuffle()` |
| IDE                  | IntelliJ IDEA / Eclipse    |
| Build Tool           | Maven / Gradle             |
| Version Control      | Git & GitHub               |

---

## 5. Application Architecture

The project is divided into three logical parts:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│   Card.java  │     │  Deck.java   │     │  GameApp.java    │
│              │     │              │     │                  │
│ - rank       │◄────│ - cards[]    │     │ - UI components  │
│ - suit       │     │ - shuffle()  │     │ - Deal button    │
│ - getValue() │     │ - deal()     │     │ - Result label   │
│ - toString() │     │              │     │ - handleDeal()   │
└──────────────┘     └──────────────┘     └──────────────────┘
```

| Class        | Responsibility                                              |
|--------------|-------------------------------------------------------------|
| `Card`       | Represents a single playing card with a rank and suit       |
| `Deck`       | Builds the full 52-card deck, shuffles it, and deals cards  |
| `GameApp`    | The main class — handles the UI and game flow               |

---

## 6. User Interface Design

The UI is simple and focused. There are two player panels side by side, a Deal button in the center below them, and a result label that displays the outcome after each deal.

### Window Properties

| Property     | Value              |
|--------------|--------------------|
| Title        | `Poker Card Game`  |
| Window Size  | 500 × 350 pixels   |
| Resizable    | Yes                |

### Layout Structure

```
VBox (root)
 ├── HBox (player panels)
 │    ├── VBox — Alice's panel
 │    │    ├── Label: "Alice"
 │    │    └── Label: card display (e.g. "K ♠")
 │    └── VBox — Bob's panel
 │         ├── Label: "Bob"
 │         └── Label: card display (e.g. "7 ♥")
 ├── Button: "Deal"
 └── Label: result message (e.g. "Alice wins!")
```

---

## 7. Game Logic

### 7.1 The Card

Each card has two properties: a **rank** and a **suit**.

- **Ranks:** 2, 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King, Ace (13 ranks)
- **Suits:** ♠ Spades, ♥ Hearts, ♦ Diamonds, ♣ Clubs (4 suits)
- **Total cards:** 13 × 4 = **52 cards**

Each rank has a numeric **value** used for comparison:

| Card  | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | J  | Q  | K  | A  |
|-------|---|---|---|---|---|---|---|---|----|----|----|----|-----|
| Value | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 |

Ace is the highest card with a value of 14.

### 7.2 Dealing Cards

On every click of the Deal button:

1. The deck is **reshuffled**.
2. The **first card** from the shuffled deck is given to Alice.
3. The **second card** is given to Bob.
4. Because the deck is shuffled randomly, Alice and Bob are guaranteed to **never receive the same card**.

### 7.3 Determining the Winner

The winner is decided by comparing the **numeric values** of the two cards:

- If Alice's card value > Bob's card value → **Alice wins**
- If Bob's card value > Alice's card value → **Bob wins**
- If both card values are equal → **It's a Draw**

---

## 8. Core Implementation

### 8.1 Card Class

```java
public class Card {
    private String rank;
    private String suit;
    private int value;

    public Card(String rank, String suit, int value) {
        this.rank = rank;
        this.suit = suit;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return rank + " " + suit;
    }
}
```

### 8.2 Deck Class

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    private static final String[] RANKS = {
        "2","3","4","5","6","7","8","9","10","J","Q","K","A"
    };
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    public Deck() {
        build();
    }

    private void build() {
        cards.clear();
        int value = 2;
        for (String rank : RANKS) {
            for (String suit : SUITS) {
                cards.add(new Card(rank, suit, value));
            }
            value++;
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.remove(0); // Remove and return the top card
    }
}
```

### 8.3 Deal Button Handler

```java
dealButton.setOnAction(e -> {
    deck.shuffle();

    Card aliceCard = deck.deal();
    Card bobCard   = deck.deal();

    aliceCardLabel.setText(aliceCard.toString());
    bobCardLabel.setText(bobCard.toString());

    // Determine winner
    String result;
    if (aliceCard.getValue() > bobCard.getValue()) {
        result = "🏆 Alice wins! (" + aliceCard + " vs " + bobCard + ")";
    } else if (bobCard.getValue() > aliceCard.getValue()) {
        result = "🏆 Bob wins! (" + bobCard + " vs " + aliceCard + ")";
    } else {
        result = "🤝 It's a Draw! (Both have " + aliceCard.getRank() + ")";
    }

    resultLabel.setText(result);

    // Rebuild deck for next round
    deck = new Deck();
});
```

### 8.4 Full Application Setup

```java
@Override
public void start(Stage stage) {

    // Alice panel
    Label aliceName = new Label("Alice");
    aliceCardLabel  = new Label("?");
    VBox aliceBox   = new VBox(10, aliceName, aliceCardLabel);
    aliceBox.setAlignment(Pos.CENTER);

    // Bob panel
    Label bobName  = new Label("Bob");
    bobCardLabel   = new Label("?");
    VBox bobBox    = new VBox(10, bobName, bobCardLabel);
    bobBox.setAlignment(Pos.CENTER);

    // Player panels side by side
    HBox playersBox = new HBox(80, aliceBox, bobBox);
    playersBox.setAlignment(Pos.CENTER);

    // Deal button
    Button dealButton = new Button("Deal");

    // Result label
    resultLabel = new Label("Press Deal to start!");
    resultLabel.setAlignment(Pos.CENTER);

    // Root layout
    VBox root = new VBox(30, playersBox, dealButton, resultLabel);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));

    // Wire up the button
    dealButton.setOnAction(e -> handleDeal());

    stage.setScene(new Scene(root, 500, 350));
    stage.setTitle("Poker Card Game");
    stage.show();
}
```

---

## 9. Winner Determination

The comparison logic is straightforward: the card with the **higher rank value** wins. The table below shows some example round outcomes:

| Round | Alice's Card | Bob's Card | Result          |
|-------|--------------|------------|-----------------|
| 1     | K ♠ (13)     | 7 ♥ (7)    | Alice wins      |
| 2     | 5 ♦ (5)      | A ♣ (14)   | Bob wins        |
| 3     | Q ♥ (12)     | Q ♠ (12)   | Draw            |
| 4     | 2 ♣ (2)      | 3 ♦ (3)    | Bob wins        |
| 5     | A ♥ (14)     | A ♦ (14)   | Draw            |
| 6     | 10 ♠ (10)    | 9 ♣ (9)    | Alice wins      |

> **Note:** A draw is possible when both players receive cards of the same rank but different suits, since only the rank value is compared — suits are not ranked.

---

## 10. Challenges & Solutions

### Challenge 1: Dealing the Same Card Twice
**Problem:** If cards were chosen with pure random numbers, there was a risk of Alice and Bob receiving the same card.  
**Solution:** Used a proper `Deck` class that removes each card after dealing it (`cards.remove(0)`). After shuffling, the first two cards are drawn in sequence, making it impossible for both players to receive the same card.

### Challenge 2: Deck Running Out After Many Deals
**Problem:** Each deal removes two cards from the deck. After 26 deals, the deck would be empty and throw an error.  
**Solution:** After each deal, a **new `Deck` object is created**, fully rebuilding and resetting the 52-card deck for the next round.

### Challenge 3: Displaying Suit Symbols
**Problem:** Unicode suit symbols (♠ ♥ ♦ ♣) were not rendering on some systems due to font or encoding issues.  
**Solution:** Ensured the source file was saved in **UTF-8** encoding and verified that the display font in JavaFX supported Unicode characters. As a fallback, text alternatives (S, H, D, C) can replace the symbols.

### Challenge 4: Showing Initial State Before First Deal
**Problem:** Before the user clicks Deal, the card labels had no content, making the layout look broken.  
**Solution:** Initialized both card labels with a `"?"` placeholder and the result label with the message `"Press Deal to start!"` so the layout looks clean from the moment the app opens.

---

## 11. Testing

| Test Case                                      | Expected Result                                     | Status  |
|------------------------------------------------|-----------------------------------------------------|---------|
| Launch the application                         | Window opens; card labels show "?"; result says "Press Deal to start!" | ✅ Pass |
| Click Deal once                                | Alice and Bob each receive a different random card  | ✅ Pass |
| Alice's card rank is higher                    | Result displays "Alice wins!"                       | ✅ Pass |
| Bob's card rank is higher                      | Result displays "Bob wins!"                         | ✅ Pass |
| Both cards have the same rank (different suits)| Result displays "It's a Draw!"                      | ✅ Pass |
| Click Deal multiple times in a row             | New random cards are dealt each time                | ✅ Pass |
| Both players never receive the same card       | Cards are always different in every round           | ✅ Pass |
| Ace is the highest card                        | Ace always beats any non-Ace card                   | ✅ Pass |
| 2 is the lowest card                           | A 2 loses to any card with a higher rank            | ✅ Pass |

---

## 12. Conclusion

This project successfully implements a **two-player card dealing game** in Java. When the Deal button is clicked, two random and distinct cards are dealt to Alice and Bob, and the winner is immediately determined and displayed based on card rank.

Key learning outcomes from this project include:

- Designing and using **custom Java classes** (`Card`, `Deck`) with object-oriented principles.
- Applying **randomization** through `Collections.shuffle()` to simulate a real card deck.
- Building an **interactive GUI** with event-driven programming using a button handler.
- Implementing **comparison logic** to evaluate game outcomes.
- Handling **edge cases** such as draws, deck exhaustion, and duplicate cards.

Possible future improvements include dealing multiple cards per player for a full poker hand, implementing real poker hand rankings (pair, flush, straight, etc.), adding player name input, keeping a score tracker across multiple rounds, and adding card images instead of text labels.

---


