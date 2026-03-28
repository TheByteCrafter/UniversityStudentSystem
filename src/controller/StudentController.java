package controller;

import model.*;
import utilis.DatabaseConnection;
import utilis.DuplicateEntryException;
import utilis.FileStorage;

import java.sql.*;
import java.util.*;

public class StudentController {
    private DatabaseConnection dbConnection;
    private FileStorage fileStorage;
    private Map<String, Student> students;
    private CourseController courseController;
    public StudentController() {
        dbConnection = DatabaseConnection.getInstance();
        fileStorage = new FileStorage("students.dat");
        courseController = new CourseController();
        students = new HashMap<>();
        loadFromFile();
        loadFromDatabase();
    }

    public void addStudent(Student student) throws DuplicateEntryException {
        // Check if student already exists in memory
        if (students.containsKey(student.getId())) {
            throw new DuplicateEntryException("Student ID already exists!", "id", student.getId());
        }

        // Check for duplicate registration number
        for (Student existing : students.values()) {
            if (existing.getRegistrationNumber().equals(student.getRegistrationNumber())) {
                throw new DuplicateEntryException("Registration number already exists!",
                        "registration_number",
                        student.getRegistrationNumber());
            }
        }

        students.put(student.getId(), student);
        saveToFile();

        try {
            saveToDatabase(student);
        } catch (RuntimeException e) {
            // If database save fails, remove from memory and rethrow
            students.remove(student.getId());
            saveToFile();
            throw new DuplicateEntryException(e.getMessage(), "database", "");
        }
    }

    public Student getStudent(String id) {
        return students.get(id);
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    public List<Course> getStudentCourses(String studentId) {
        Student student = students.get(studentId);
        return student != null ? student.getEnrolledCourses() : new ArrayList<>();
    }

    public void enrollStudentInCourse(String studentId, Course course) {
        Student student = students.get(studentId);
        if (student != null && student.getEnrolledCourses().size() < 5) {
            student.enrollCourse(course);
            saveToFile();
            updateDatabaseEnrollment(studentId, course.getCourseCode());
        }
    }

    public void refreshFromDatabase() {
        students.clear();
        loadFromDatabase();
        System.out.println("Students refreshed from database. Total: " + students.size());
    }
    public Student getStudentByRegistrationNumber(String registrationNumber) {
        for (Student student : students.values()) {
            if (student.getRegistrationNumber().equalsIgnoreCase(registrationNumber)) {
                return student;
            }
        }
        return null;
    }

    private void saveToDatabase(Student student) {
        String query = "INSERT INTO students (id, name, email, phone, registration_number, " +
                "programme, year_of_study) VALUES ('" + student.getId() + "', '" +
                student.getName() + "', '" + student.getEmail() + "', '" +
                student.getPhone() + "', '" + student.getRegistrationNumber() + "', '" +
                student.getProgramme() + "', " + student.getYearOfStudy() + ")";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                String errorMessage = e.getMessage();
                if (errorMessage.contains("PRIMARY")) {
                    throw new RuntimeException("Student ID already exists: " + student.getId());
                } else if (errorMessage.contains("registration_number")) {
                    throw new RuntimeException("Registration number already exists: " +
                            student.getRegistrationNumber());
                } else {
                    throw new RuntimeException("Duplicate entry detected. Please check unique fields.");
                }
            } else {
                e.printStackTrace();
                throw new RuntimeException("Database error: " + e.getMessage());
            }
        }
    }

    private void updateDatabaseEnrollment(String studentId, String courseCode) {
        String query = "INSERT INTO enrollments (student_id, course_code) VALUES ('" +
                studentId + "', '" + courseCode + "')";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        String query = "SELECT * FROM students";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("registration_number"),
                        rs.getString("programme"),
                        rs.getInt("year_of_study")
                );
                students.put(student.getId(), student);
            }

            // Load enrollments for all students
            loadEnrollments();

            System.out.println("Loaded " + students.size() + " students from database");
        } catch (SQLException e) {
            System.err.println("Error loading students from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadEnrollments() {
        String query = "SELECT student_id, course_code FROM enrollments";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String courseCode = rs.getString("course_code");

                Student student = students.get(studentId);
                Course course = courseController.getCourse(courseCode);

                if (student != null && course != null) {
                    student.enrollCourse(course);
                }
            }
            System.out.println("Loaded enrollments successfully");
        } catch (SQLException e) {
            System.err.println("Error loading enrollments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStudent(Student student) {
        students.put(student.getId(), student);
        saveToFile();
        updateDatabaseStudent(student);
    }

    public void deleteStudent(String id) {
        students.remove(id);
        saveToFile();
        deleteDatabaseStudent(id);
    }

    public void removeStudentFromCourse(String studentId, String courseCode) {
        Student student = students.get(studentId);
        if (student != null) {
            student.getEnrolledCourses().removeIf(c -> c.getCourseCode().equals(courseCode));
            saveToFile();
            deleteDatabaseEnrollment(studentId, courseCode);
        }
    }

    private void updateDatabaseStudent(Student student) {
        String query = "UPDATE students SET name='" + student.getName() +
                "', email='" + student.getEmail() +
                "', phone='" + student.getPhone() +
                "', programme='" + student.getProgramme() +
                "', year_of_study=" + student.getYearOfStudy() +
                " WHERE id='" + student.getId() + "'";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabaseStudent(String id) {
        String query = "DELETE FROM students WHERE id='" + id + "'";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabaseEnrollment(String studentId, String courseCode) {
        String query = "DELETE FROM enrollments WHERE student_id='" + studentId +
                "' AND course_code='" + courseCode + "'";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void saveToFile() {
        fileStorage.save(students);
    }

    private void loadFromFile() {
        Object obj = fileStorage.load();
        if (obj instanceof Map) {
            students = (Map<String, Student>) obj;
        }
    }
}