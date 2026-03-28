package controller;

import model.*;
import utilis.DatabaseConnection;
import utilis.DuplicateEntryException;
import utilis.FileStorage;

import java.sql.*;
import java.util.*;

public class CourseController {
    private DatabaseConnection dbConnection;
    private FileStorage fileStorage;
    private Map<String, Course> courses;

    public CourseController() {
        dbConnection = DatabaseConnection.getInstance();
        fileStorage = new FileStorage("courses.dat");
        courses = new HashMap<>();
        loadFromFile();
        loadFromDatabase();
    }

    public void addCourse(Course course) throws DuplicateEntryException {
        // Check if course already exists in memory
        if (courses.containsKey(course.getCourseCode())) {
            throw new DuplicateEntryException("Course code already exists!",
                    "course_code", course.getCourseCode());
        }

        courses.put(course.getCourseCode(), course);
        saveToFile();

        try {
            saveToDatabase(course);
        } catch (RuntimeException e) {
            // If database save fails, remove from memory and rethrow
            courses.remove(course.getCourseCode());
            saveToFile();
            throw new DuplicateEntryException(e.getMessage(), "database", "");
        }
    }

    public void updateCourse(Course course) {
        courses.put(course.getCourseCode(), course);
        saveToFile();
        updateDatabaseCourse(course);
    }

    public void deleteCourse(String courseCode) {
        courses.remove(courseCode);
        saveToFile();
        deleteDatabaseCourse(courseCode);
    }

    public Course getCourse(String courseCode) {
        return courses.get(courseCode);
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses.values());
    }

    public List<Course> getCoursesByLecturer(String lecturerId) {
        List<Course> lecturerCourses = new ArrayList<>();
        for (Course course : courses.values()) {
            if (lecturerId.equals(course.getLecturerId())) {
                lecturerCourses.add(course);
            }
        }
        return lecturerCourses;
    }

    public List<Course> getAvailableCoursesForStudent(String studentId, List<Course> enrolledCourses) {
        List<Course> availableCourses = new ArrayList<>();
        for (Course course : courses.values()) {
            boolean isEnrolled = false;
            for (Course enrolled : enrolledCourses) {
                if (enrolled.getCourseCode().equals(course.getCourseCode())) {
                    isEnrolled = true;
                    break;
                }
            }
            if (!isEnrolled) {
                availableCourses.add(course);
            }
        }
        return availableCourses;
    }


    // Add this method for refreshing from database
    public void refreshFromDatabase() {
        courses.clear();
        loadFromDatabase();
        System.out.println("Courses refreshed from database. Total: " + courses.size());
    }

    private void saveToDatabase(Course course) {
        String query = "INSERT INTO courses (course_code, title, credit_hours, lecturer_id) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCreditHours());
            pstmt.setString(4, course.getLecturerId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RuntimeException("Course code already exists: " + course.getCourseCode());
            }
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    private void updateDatabaseCourse(Course course) {
        String query = "UPDATE courses SET title=?, credit_hours=?, lecturer_id=? " +
                "WHERE course_code=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, course.getTitle());
            pstmt.setInt(2, course.getCreditHours());
            pstmt.setString(3, course.getLecturerId());
            pstmt.setString(4, course.getCourseCode());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabaseCourse(String courseCode) {
        String query = "DELETE FROM courses WHERE course_code=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, courseCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        String query = "SELECT * FROM courses";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Course course = new Course(
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getInt("credit_hours"),
                        rs.getString("lecturer_id")
                );
                courses.put(course.getCourseCode(), course);
            }
            System.out.println("Loaded " + courses.size() + " courses from database");
        } catch (SQLException e) {
            System.err.println("Error loading courses from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        fileStorage.save(courses);
    }

    private void loadFromFile() {
        Object obj = fileStorage.load();
        if (obj instanceof Map) {
            courses = (Map<String, Course>) obj;
            System.out.println("Loaded " + courses.size() + " courses from file");
        }
    }
}