//be inside AP file
//javac poker_game\src\PokerGameFX.java
 //java -cp poker_game\src poker.PokerGameFX


// package pokerr.poker;
// import java.io.*;
// import java.util.*;
// import java.util.stream.Collectors;

// public class PokerGame implements Serializable {

//     // ==========================
//     // Card Class
//     // ==========================
//     static class Card implements Serializable {
//         private String suit;
//         private String rank;
//         private int value;

//         public Card(String suit, String rank, int value) {
//             this.suit = suit;
//             this.rank = rank;
//             this.value = value;
//         }

//         public int getValue() {
//             return value;
//         }

//         @Override
//         public String toString() {
//             return rank + " of " + suit;
//         }
//     }

//     // ==========================
//     // Player Class
//     // ==========================
//     static class Player implements Serializable {
//         private String name;
//         private List<Card> hand = new ArrayList<>();

//         public Player(String name) {
//             this.name = name;
//         }

//         public void addCard(Card card) {
//             hand.add(card);
//         }

//         public int getHighestCard() {
//             return hand.stream()
//                     .map(Card::getValue)
//                     .max(Integer::compare)
//                     .orElse(0);
//         }

//         public void showHand() {
//             System.out.println(name + "'s Hand:");
//             hand.forEach(System.out::println);
//             System.out.println();
//         }

//         public String getName() {
//             return name;
//         }
//     }

//     // ==========================
//     // Deck Class
//     // ==========================
//     static class Deck {
//         private List<Card> cards = new ArrayList<>();

//         public Deck() {
//             String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
//             String[] ranks = {"2","3","4","5","6","7","8","9","10",
//                     "Jack","Queen","King","Ace"};

//             for (String suit : suits) {
//                 for (int i = 0; i < ranks.length; i++) {
//                     cards.add(new Card(suit, ranks[i], i + 2));
//                 }
//             }

//             Collections.shuffle(cards);
//         }

//         public Card dealCard() {
//             return cards.remove(0);
//         }
//     }

//     // ==========================
//     // Game Logic
//     // ==========================
//     private List<Player> players = new ArrayList<>();

//     public PokerGame() {
//         players.add(new Player("Alice"));
//         players.add(new Player("Bob"));
//     }

//     public void startGame() {
//         Deck deck = new Deck();

//         // Deal 5 cards to each player
//         for (int i = 0; i < 5; i++) {
//             players.forEach(p -> p.addCard(deck.dealCard()));
//         }

//         // Show all hands
//         players.forEach(Player::showHand);

//         // Find winner using lambda + comparator
//         Player winner = players.stream()
//                 .max(Comparator.comparing(Player::getHighestCard))
//                 .get();

//         System.out.println("Winner is: " + winner.getName());

//         saveGame();
//     }

//     // ==========================
//     // Serialization
//     // ==========================
//     private void saveGame() {
//         try (ObjectOutputStream out =
//                      new ObjectOutputStream(new FileOutputStream("poker.ser"))) {
//             out.writeObject(this);
//             System.out.println("Game saved successfully.");
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     // ==========================
//     // Main Method
//     // ==========================
//     public static void main(String[] args) {
//         PokerGame game = new PokerGame();
//         game.startGame();
//     }
// }
// // javac PokerGame.java
// //java PokerGame






import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

// We named the class PokerGameFX to match your file name
public class PokerGameFX extends JFrame {

    static class Card implements Serializable {
        String suit, rank;
        int value;
        public Card(String suit, String rank, int value) {
            this.suit = suit; this.rank = rank; this.value = value;
        }
        @Override
        public String toString() { return rank + " of " + suit; }
    }

    static class Player {
        String name;
        List<Card> hand = new ArrayList<>();
        public Player(String name) { this.name = name; }
        public int getHighestCard() {
            return hand.stream().map(c -> c.value).max(Integer::compare).orElse(0);
        }
    }

    static class Deck {
        List<Card> cards = new ArrayList<>();
        public Deck() {
            String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
            String[] ranks = {"2","3","4","5","6","7","8","9","10","Jack","Queen","King","Ace"};
            for (String suit : suits) {
                for (int i = 0; i < ranks.length; i++) cards.add(new Card(suit, ranks[i], i + 2));
            }
            Collections.shuffle(cards);
        }
        public Card dealCard() { return cards.remove(0); }
    }

    private JTextArea aliceArea = new JTextArea(10, 15);
    private JTextArea bobArea = new JTextArea(10, 15);
    private JLabel resultLabel = new JLabel("Click Deal to Start!", SwingConstants.CENTER);

    public PokerGameFX() {
        setTitle("Poker High Card Game");
        setSize(500, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        // Create UI
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        aliceArea.setEditable(false);
        bobArea.setEditable(false);
        aliceArea.setBackground(new Color(240, 240, 240));
        bobArea.setBackground(new Color(240, 240, 240));

        JPanel aBox = new JPanel(new BorderLayout());
        aBox.add(new JLabel("Alice's Hand:"), BorderLayout.NORTH);
        aBox.add(new JScrollPane(aliceArea), BorderLayout.CENTER);

        JPanel bBox = new JPanel(new BorderLayout());
        bBox.add(new JLabel("Bob's Hand:"), BorderLayout.NORTH);
        bBox.add(new JScrollPane(bobArea), BorderLayout.CENTER);

        centerPanel.add(aBox);
        centerPanel.add(bBox);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton dealBtn = new JButton("DEAL CARDS");
        dealBtn.setFont(new Font("Arial", Font.BOLD, 14));
        
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        dealBtn.addActionListener(e -> playRound());

        bottomPanel.add(dealBtn);
        bottomPanel.add(resultLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        // UI padding
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    private void playRound() {
        Deck deck = new Deck();
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");

        aliceArea.setText("");
        bobArea.setText("");

        for (int i = 0; i < 5; i++) {
            alice.hand.add(deck.dealCard());
            bob.hand.add(deck.dealCard());
        }

        alice.hand.forEach(c -> aliceArea.append(c + "\n"));
        bob.hand.forEach(c -> bobArea.append(c + "\n"));

        if (alice.getHighestCard() > bob.getHighestCard()) {
            resultLabel.setText("WINNER: ALICE!");
            resultLabel.setForeground(Color.BLUE);
        } else if (bob.getHighestCard() > alice.getHighestCard()) {
            resultLabel.setText("WINNER: BOB!");
            resultLabel.setForeground(Color.RED);
        } else {
            resultLabel.setText("IT'S A TIE!");
            resultLabel.setForeground(Color.BLACK);
        }
    }

    public static void main(String[] args) {
        // Runs the GUI
        SwingUtilities.invokeLater(() -> new PokerGameFX());
    }
}