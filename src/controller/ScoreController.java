package controller;

import model.*;
import utilis.DatabaseConnection;
import utilis.FileStorage;

import java.sql.*;
import java.util.*;

public class ScoreController {
    private DatabaseConnection dbConnection;
    private FileStorage fileStorage;
    private Map<String, List<Score>> scores;

    public ScoreController() {
        dbConnection = DatabaseConnection.getInstance();
        fileStorage = new FileStorage("scores.dat");
        scores = new HashMap<>();
        loadFromFile();
        loadFromDatabase();
    }

    public void addScore(Score score) {
        scores.computeIfAbsent(score.getStudentId(), k -> new ArrayList<>()).add(score);
        saveToFile();
        saveToDatabase(score);
    }

    public List<Score> getStudentScores(String studentId) {
        return scores.getOrDefault(studentId, new ArrayList<>());
    }

    public Score getStudentCourseScore(String studentId, String courseCode) {
        List<Score> studentScores = scores.get(studentId);
        if (studentScores != null) {
            return studentScores.stream()
                    .filter(s -> s.getCourseCode().equals(courseCode))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    public void refreshFromDatabase() {
        scores.clear();
        loadFromDatabase();
        System.out.println("Students refreshed from database. Total: " + scores.size());
    }
    private void saveToDatabase(Score score) {
        String query = "INSERT INTO scores (student_id, course_code, cat_score, exam_score) " +
                "VALUES ('" + score.getStudentId() + "', '" + score.getCourseCode() + "', " +
                score.getCatScore() + ", " + score.getExamScore() + ")";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        String query = "SELECT * FROM scores";
        try {
            ResultSet rs = dbConnection.executeQuery(query);
            while (rs.next()) {
                Score score = new Score(
                        rs.getDouble("cat_score"),
                        rs.getDouble("exam_score"),
                        rs.getString("course_code"),
                        rs.getString("student_id")
                );
                scores.computeIfAbsent(score.getStudentId(), k -> new ArrayList<>()).add(score);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateScore(Score score) {
        List<Score> studentScores = scores.get(score.getStudentId());
        if (studentScores != null) {
            for (int i = 0; i < studentScores.size(); i++) {
                if (studentScores.get(i).getCourseCode().equals(score.getCourseCode())) {
                    studentScores.set(i, score);
                    break;
                }
            }
        }
        saveToFile();
        updateDatabaseScore(score);
    }

    public void deleteScore(String studentId, String courseCode) {
        List<Score> studentScores = scores.get(studentId);
        if (studentScores != null) {
            studentScores.removeIf(s -> s.getCourseCode().equals(courseCode));
            saveToFile();
            deleteDatabaseScore(studentId, courseCode);
        }
    }

    private void updateDatabaseScore(Score score) {
        String query = "UPDATE scores SET cat_score=" + score.getCatScore() +
                ", exam_score=" + score.getExamScore() +
                " WHERE student_id='" + score.getStudentId() +
                "' AND course_code='" + score.getCourseCode() + "'";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDatabaseScore(String studentId, String courseCode) {
        String query = "DELETE FROM scores WHERE student_id='" + studentId +
                "' AND course_code='" + courseCode + "'";
        try {
            dbConnection.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        fileStorage.save(scores);
    }

    private void loadFromFile() {
        Object obj = fileStorage.load();
        if (obj instanceof Map) {
            scores = (Map<String, List<Score>>) obj;
        }
    }
}