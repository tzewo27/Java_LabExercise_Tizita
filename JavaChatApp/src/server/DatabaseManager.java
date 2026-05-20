// package server;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;

// public class DatabaseManager {
//     // XAMPP Default credentials
//     private static final String URL = "jdbc:mysql://localhost:3306/ChatDB";
//     private static final String USER = "root";
//     private static final String PASS = "";

//     public static Connection getConnection() throws SQLException {
//         return DriverManager.getConnection(URL, USER, PASS);
//     }

//     // This method will be used later to save messages
//     // public static void saveMessage(String sender, String content) {
//     //     String query = "INSERT INTO messages (sender, content) VALUES (?, ?)";
//     //     try (Connection conn = getConnection();
//     //          PreparedStatement pst = conn.prepareStatement(query)) {
//     //         pst.setString(1, sender);
//     //         pst.setString(2, content);
//     //         pst.executeUpdate();
//     //     } catch (SQLException e) {
//     //         e.printStackTrace();
//     //     }
//     // }




//     public static void saveMessage(String sender, String content, String fileName, byte[] fileData) {
//         String query = "INSERT INTO messages (sender, content, file_name, file_data) VALUES (?, ?, ?, ?)";
//         try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
//              PreparedStatement pst = conn.prepareStatement(query)) {
//             pst.setString(1, sender);
//             pst.setString(2, content);
//             pst.setString(3, fileName);
//             pst.setBytes(4, fileData); // Stores the raw bytes of the file
//             pst.executeUpdate();
//         } catch (SQLException e) { e.printStackTrace(); }
//     }
// }


package server;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/ChatDB";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void saveMessage(String sender, String content, String fileName, byte[] fileData) {
        String query = "INSERT INTO messages (sender, content, file_name, file_data) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, sender);
            pst.setString(2, content);
            pst.setString(3, fileName);
            if (fileData != null) {
                pst.setBytes(4, fileData);
            } else {
                pst.setNull(4, Types.BLOB);
            }
            pst.executeUpdate();
            System.out.println("Saved to Database: " + (fileName != null ? "FILE " + fileName : "TEXT"));
        } catch (SQLException e) {
            System.out.println("Database Save Error: " + e.getMessage());
        }
    }
}



