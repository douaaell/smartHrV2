package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/smartHrV2_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver non trouvé", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("🔄 Test de connexion à la base de données...");
        
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connexion réussie à la base de données !");
            System.out.println("Base de données : smartHrV2_db");
            System.out.println("Utilisateur : " + USER);
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}