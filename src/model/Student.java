package model;

import utilis.GradeCalculator;

import java.util.*;

public class Student extends Person {
    private String registrationNumber;
    private String programme;
    private int yearOfStudy;
    private List<Course> enrolledCourses;
    private Map<String, Score> scores;
    private List<BorrowRecord> borrowedBooks;

    public Student(String id, String name, String email, String phone,
                   String registrationNumber, String programme, int yearOfStudy) {
        super(id, name, email, phone);
        this.registrationNumber = registrationNumber;
        this.programme = programme;
        this.yearOfStudy = yearOfStudy;
        this.enrolledCourses = new ArrayList<>();
        this.scores = new HashMap<>();
        this.borrowedBooks = new ArrayList<>();
    }

    public void enrollCourse(Course course) {
        if (enrolledCourses.size() < 5 && !enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
        }
    }

    public void addScore(String courseCode, Score score) {
        scores.put(courseCode, score);
    }

    public double calculateAverage() {
        if (scores.isEmpty()) return 0.0;
        double total = 0.0;
        for (Score score : scores.values()) {
            total += score.getTotal();
        }
        return total / scores.size();
    }

    public String getAverageGrade() {
        return GradeCalculator.getGrade(calculateAverage());
    }

    // Getters and Setters
    public String getRegistrationNumber() { return registrationNumber; }
    public String getProgramme() { return programme; }
    public int getYearOfStudy() { return yearOfStudy; }
    public List<Course> getEnrolledCourses() { return enrolledCourses; }
    public Map<String, Score> getScores() { return scores; }
    public List<BorrowRecord> getBorrowedBooks() { return borrowedBooks; }

    @Override
    public String toString() {
        return registrationNumber;
    }
}