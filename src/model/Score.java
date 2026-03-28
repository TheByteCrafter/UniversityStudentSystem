package model;

import java.io.Serializable;

public class Score implements Serializable {
    private double catScore;
    private double examScore;
    private String courseCode;
    private String studentId;

    public Score(double catScore, double examScore, String courseCode, String studentId) {
        this.catScore = Math.min(catScore, 30);
        this.examScore = Math.min(examScore, 70);
        this.courseCode = courseCode;
        this.studentId = studentId;
    }

    public double getTotal() {
        return catScore + examScore;
    }

    public String getGrade() {
        double total = getTotal();
        if (total >= 70) return "A";
        else if (total >= 60) return "B";
        else if (total >= 50) return "C";
        else if (total >= 40) return "D";
        else return "F";
    }

    // Getters and Setters
    public double getCatScore() { return catScore; }
    public double getExamScore() { return examScore; }
    public String getCourseCode() { return courseCode; }
    public String getStudentId() { return studentId; }
}