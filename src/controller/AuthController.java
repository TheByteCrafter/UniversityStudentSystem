package controller;


import utilis.DatabaseConnection;

import java.sql.*;

public class AuthController {
    private DatabaseConnection dbConnection;

    public AuthController() {
        dbConnection = DatabaseConnection.getInstance();
    }

    public boolean authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username='" + username +
                "' AND password='" + password + "'";
        try {
            ResultSet rs = dbConnection.executeQuery(query);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username='" + username + "'";
        try {
            ResultSet rs = dbConnection.executeQuery(query);
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "STUDENT"; // Default role
    }
}