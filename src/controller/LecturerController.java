package controller;

import model.*;
import utilis.DatabaseConnection;
import utilis.FileStorage;
import utilis.DuplicateEntryException;
import java.sql.*;
import java.util.*;

public class LecturerController {
    private DatabaseConnection dbConnection;
    private FileStorage fileStorage;
    private Map<String, Lecturer> lecturers;
    private CourseController courseController;

    public LecturerController() {
        dbConnection = DatabaseConnection.getInstance();
        fileStorage = new FileStorage("lecturers.dat");
        lecturers = new HashMap<>();
        courseController = new CourseController();
        loadFromFile();
        loadFromDatabase();
    }

    public void addLecturer(Lecturer lecturer) throws DuplicateEntryException {
        // Check if lecturer already exists in memory
        if (lecturers.containsKey(lecturer.getId())) {
            throw new DuplicateEntryException("Lecturer ID already exists!", "id", lecturer.getId());
        }

        // Check for duplicate staff number
        for (Lecturer existing : lecturers.values()) {
            if (existing.getStaffNumber().equals(lecturer.getStaffNumber())) {
                throw new DuplicateEntryException("Staff number already exists!",
                        "staff_number", lecturer.getStaffNumber());
            }
        }

        lecturers.put(lecturer.getId(), lecturer);
        saveToFile();

        try {
            saveToDatabase(lecturer);
        } catch (RuntimeException e) {
            lecturers.remove(lecturer.getId());
            saveToFile();
            throw new DuplicateEntryException(e.getMessage(), "database", "");
        }
    }

    public void updateLecturer(Lecturer lecturer) {
        lecturers.put(lecturer.getId(), lecturer);
        saveToFile();
        updateDatabaseLecturer(lecturer);
    }

    public void deleteLecturer(String id) {
        lecturers.remove(id);
        saveToFile();
        deleteDatabaseLecturer(id);
    }

    public Lecturer getLecturer(String id) {
        return lecturers.get(id);
    }

    public List<Lecturer> getAllLecturers() {
        return new ArrayList<>(lecturers.values());
    }

    public void assignCourseToLecturer(String lecturerId, Course course) {
        Lecturer lecturer = lecturers.get(lecturerId);
        if (lecturer != null) {
            // Check if already assigned
            for (Course assigned : lecturer.getAssignedCourses()) {
                if (assigned.getCourseCode().equals(course.getCourseCode())) {
                    return; // Already assigned
                }
            }

            lecturer.assignCourse(course);
            course.setLecturerId(lecturerId);
            saveToFile();
            updateDatabaseAssignment(lecturerId, course.getCourseCode());
        }
    }

    public void removeCourseFromLecturer(String lecturerId, String courseCode) {
        Lecturer lecturer = lecturers.get(lecturerId);
        if (lecturer != null) {
            lecturer.getAssignedCourses().removeIf(c -> c.getCourseCode().equals(courseCode));
            saveToFile();
            updateDatabaseRemoveAssignment(lecturerId, courseCode);
        }
    }

    public void refreshFromDatabase() {
        lecturers.clear();
        loadFromDatabase();
    }

    private void saveToDatabase(Lecturer lecturer) {
        String query = "INSERT INTO lecturers (id, name, email, phone, staff_number, department) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, lecturer.getId());
            pstmt.setString(2, lecturer.getName());
            pstmt.setString(3, lecturer.getEmail());
            pstmt.setString(4, lecturer.getPhone());
            pstmt.setString(5, lecturer.getStaffNumber());
            pstmt.setString(6, lecturer.getDepartment());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Lecturer ID or Staff Number already exists!");
            }
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    private void updateDatabaseLecturer(Lecturer lecturer) {
        String query = "UPDATE lecturers SET name=?, email=?, phone=?, department=? " +
                "WHERE id=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, lecturer.getName());
            pstmt.setString(2, lecturer.getEmail());
            pstmt.setString(3, lecturer.getPhone());
            pstmt.setString(4, lecturer.getDepartment());
            pstmt.setString(5, lecturer.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabaseLecturer(String id) {
        String query = "DELETE FROM lecturers WHERE id=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDatabaseAssignment(String lecturerId, String courseCode) {
        String query = "UPDATE courses SET lecturer_id=? WHERE course_code=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, lecturerId);
            pstmt.setString(2, courseCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDatabaseRemoveAssignment(String lecturerId, String courseCode) {
        String query = "UPDATE courses SET lecturer_id=NULL WHERE course_code=? AND lecturer_id=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, courseCode);
            pstmt.setString(2, lecturerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        // Load lecturers
        String query = "SELECT * FROM lecturers";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Lecturer lecturer = new Lecturer(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("staff_number"),
                        rs.getString("department")
                );
                lecturers.put(lecturer.getId(), lecturer);
            }

            // Load assigned courses for all lecturers
            loadAssignedCourses();

            System.out.println("Loaded " + lecturers.size() + " lecturers from database");
        } catch (SQLException e) {
            System.err.println("Error loading lecturers from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAssignedCourses() {
        String query = "SELECT course_code, lecturer_id FROM courses WHERE lecturer_id IS NOT NULL";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String lecturerId = rs.getString("lecturer_id");

                Lecturer lecturer = lecturers.get(lecturerId);
                Course course = courseController.getCourse(courseCode);

                if (lecturer != null && course != null) {
                    lecturer.assignCourse(course);
                }
            }
            System.out.println("Loaded assigned courses successfully");
        } catch (SQLException e) {
            System.err.println("Error loading assigned courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        fileStorage.save(lecturers);
    }

    private void loadFromFile() {
        Object obj = fileStorage.load();
        if (obj instanceof Map) {
            lecturers = (Map<String, Lecturer>) obj;
            System.out.println("Loaded " + lecturers.size() + " lecturers from file");
        }
    }
}