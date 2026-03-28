package views;
import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private String userRole;
    private JTabbedPane tabbedPane;

    public MainView(String role) {
        this.userRole = role;
        initUI();
    }

    private void initUI() {
        setTitle("Student Management System - " + userRole);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Add tabs based on user role
        if (userRole.equals("ADMIN") || userRole.equals("LECTURER")) {
            tabbedPane.addTab("Students", new StudentView());
            tabbedPane.addTab("Lecturers", new LecturerView());
            tabbedPane.addTab("Courses", new CourseView());
            tabbedPane.addTab("Scores", new ScoreView());
        }

        if (userRole.equals("ADMIN") || userRole.equals("STUDENT")) {
            tabbedPane.addTab("My Courses", new StudentCoursesView());
            tabbedPane.addTab("Results", new ResultSlipView());
        }

        tabbedPane.addTab("Books", new BookView());

        add(tabbedPane);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginView().setVisible(true);
        }
    }
}