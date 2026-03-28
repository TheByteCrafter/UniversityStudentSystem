package utilis;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url = "jdbc:mysql://127.0.0.1:3306/student_system";
    private String username = "root";
    private String password = "1234";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            connection = DriverManager.getConnection(url, props);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeUpdate(String query) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // New method to initialize database schema
    public static void initializeSchema() {
        Connection initConn = null;
        Statement stmt = null;

        try {
            // First connect without specifying database
            Class.forName("com.mysql.cj.jdbc.Driver");
            String baseUrl = "jdbc:mysql://127.0.0.1/";
            Properties props = new Properties();
            props.setProperty("user", "root");
            props.setProperty("password", "1234");
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");

            initConn = DriverManager.getConnection(baseUrl, props);
            stmt = initConn.createStatement();

            // Create database if it doesn't exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS student_system");
            stmt.executeUpdate("USE student_system");

            // Create all tables
            createTables(stmt);

            // Insert default data if needed
            insertDefaultData(stmt);

            System.out.println("Database schema initialized successfully!");

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to initialize database schema!");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (initConn != null) initConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        // Users table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "username VARCHAR(50) PRIMARY KEY," +
                        "password VARCHAR(255) NOT NULL," +
                        "role VARCHAR(20) NOT NULL," +
                        "person_id VARCHAR(20)" +
                        ")"
        );

        // Students table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS students (" +
                        "id VARCHAR(20) PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL," +
                        "email VARCHAR(100)," +
                        "phone VARCHAR(15)," +
                        "registration_number VARCHAR(20) UNIQUE," +
                        "programme VARCHAR(50)," +
                        "year_of_study INT" +
                        ")"
        );

        // Lecturers table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS lecturers (" +
                        "id VARCHAR(20) PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL," +
                        "email VARCHAR(100)," +
                        "phone VARCHAR(15)," +
                        "staff_number VARCHAR(20) UNIQUE," +
                        "department VARCHAR(50)" +
                        ")"
        );

        // Courses table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS courses (" +
                        "course_code VARCHAR(10) PRIMARY KEY," +
                        "title VARCHAR(100) NOT NULL," +
                        "credit_hours INT," +
                        "lecturer_id VARCHAR(20)," +
                        "FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE SET NULL" +
                        ")"
        );

        // Enrollments table - FIXED
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS enrollments (" +
                        "student_id VARCHAR(20)," +
                        "course_code VARCHAR(10)," +
                        "enrollment_date DATE," +
                        "PRIMARY KEY (student_id, course_code)," +
                        "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE" +
                        ")"
        );

        // Scores table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS scores (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "student_id VARCHAR(20)," +
                        "course_code VARCHAR(10)," +
                        "cat_score DECIMAL(5,2)," +
                        "exam_score DECIMAL(5,2)," +
                        "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE," +
                        "UNIQUE KEY unique_student_course (student_id, course_code)" +
                        ")"
        );

        // Books table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books (" +
                        "isbn VARCHAR(20) PRIMARY KEY," +
                        "title VARCHAR(200) NOT NULL," +
                        "edition INT," +
                        "version VARCHAR(20)," +
                        "year_published INT," +
                        "total_copies INT DEFAULT 1," +
                        "borrowed_copies INT DEFAULT 0" +
                        ")"
        );

        // Borrow Records table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS borrow_records (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "book_isbn VARCHAR(20)," +
                        "student_id VARCHAR(20)," +
                        "borrow_date DATE NOT NULL," +
                        "due_date DATE NOT NULL," +
                        "return_date DATE," +
                        "FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE CASCADE," +
                        "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE" +
                        ")"
        );

        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reservations (\n" +
                        "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                        "    book_isbn VARCHAR(20),\n" +
                        "    student_id VARCHAR(20),\n" +
                        "    reservation_date DATE NOT NULL,\n" +
                        "    FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE CASCADE,\n" +
                        "    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,\n" +
                        "    UNIQUE KEY unique_reservation (book_isbn, student_id)\n" +
                        ")"
        );

        System.out.println("All tables created/verified successfully!");
    }
    private static void insertDefaultData(Statement stmt) throws SQLException {
        // Check if users table is empty
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        rs.next();
        int userCount = rs.getInt(1);

        if (userCount == 0) {
            // Insert default admin user
            stmt.executeUpdate(
                    "INSERT INTO users (username, password, role) VALUES " +
                            "('admin', 'admin123', 'ADMIN')"
            );

            // Insert default lecturer
            stmt.executeUpdate(
                    "INSERT INTO lecturers (id, name, email, phone, staff_number, department) VALUES " +
                            "('LEC001', 'Dr. Jane Smith', 'jane.smith@chuka.ac.ke', '0723456789', 'STAFF001', 'Computer Science')"
            );
            stmt.executeUpdate(
                    "INSERT INTO users (username, password, role, person_id) VALUES " +
                            "('lecturer1', 'pass123', 'LECTURER', 'LEC001')"
            );

            // Insert default student
            stmt.executeUpdate(
                    "INSERT INTO students (id, name, email, phone, registration_number, programme, year_of_study) VALUES " +
                            "('STU001', 'John Doe', 'john.doe@chuka.ac.ke', '0712345678', 'REG001', 'Computer Science', 2)"
            );
            stmt.executeUpdate(
                    "INSERT INTO users (username, password, role, person_id) VALUES " +
                            "('student1', 'pass123', 'STUDENT', 'STU001')"
            );

            // Insert default courses
            stmt.executeUpdate(
                    "INSERT INTO courses (course_code, title, credit_hours, lecturer_id) VALUES " +
                            "('COSC223', 'Object Oriented Programming', 3, 'LEC001')," +
                            "('COSC224', 'Database Systems', 3, 'LEC001')," +
                            "('MATH201', 'Discrete Mathematics', 3, NULL)"
            );

            // Insert default books
            stmt.executeUpdate(
                    "INSERT INTO books (isbn, title, edition, version, year_published, total_copies) VALUES " +
                            "('9780135166307', 'Java: How to Program', 11, '11th', 2017, 5)," +
                            "('9780134685991', 'Effective Java', 3, '3rd', 2018, 3)," +
                            "('9781492078007', 'Head First Java', 3, '3rd', 2022, 4)"
            );

            // Enroll student in courses
            stmt.executeUpdate(
                    "INSERT INTO enrollments (student_id, course_code) VALUES " +
                            "('STU001', 'COSC223')," +
                            "('STU001', 'COSC224')"
            );

            System.out.println("Default data inserted successfully!");
        }

        rs.close();
    }
}