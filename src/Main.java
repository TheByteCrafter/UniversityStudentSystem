
import utilis.DatabaseConnection;
import views.LoginView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize database schema before starting the application
        System.out.println("Checking/Initializing database schema...");
        DatabaseConnection.initializeSchema();

        // Run the GUI on the Event
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}