package model;

import java.io.Serializable;

public class Course implements Serializable {
    private String courseCode;
    private String title;
    private int creditHours;
    private String lecturerId;

    public Course(String courseCode, String title, int creditHours, String lecturerId) {
        this.courseCode = courseCode;
        this.title = title;
        this.creditHours = creditHours;
        this.lecturerId = lecturerId;
    }

    // Getters and Setters
    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public int getCreditHours() { return creditHours; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }

    @Override
    public String toString() {
        return courseCode + " - " + title;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseCode != null && courseCode.equals(course.courseCode);
    }

    @Override
    public int hashCode() {
        return courseCode != null ? courseCode.hashCode() : 0;
    }
}