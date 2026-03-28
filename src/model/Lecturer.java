package model;

import java.util.*;

public class Lecturer extends Person {
    private String staffNumber;
    private String department;
    private List<Course> assignedCourses;

    public Lecturer(String id, String name, String email, String phone,
                    String staffNumber, String department) {
        super(id, name, email, phone);
        this.staffNumber = staffNumber;
        this.department = department;
        this.assignedCourses = new ArrayList<>();
    }

    public void assignCourse(Course course) {
        if (!assignedCourses.contains(course)) {
            assignedCourses.add(course);
        }
    }

    // Getters and Setters
    public String getStaffNumber() { return staffNumber; }
    public String getDepartment() { return department; }
    public List<Course> getAssignedCourses() { return assignedCourses; }

    @Override
    public String toString() {
        return name;
    }
}