package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import controller.StudentController;
import controller.ScoreController;
import model.Student;
import model.Course;
import model.Score;

public class StudentCoursesView extends JPanel {
    private JComboBox<Student> studentCombo;
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private StudentController studentController;
    private ScoreController scoreController;
    private String currentStudentId;

    public StudentCoursesView() {
        studentController = new StudentController();
        scoreController = new ScoreController();
        initUI();
        loadStudents();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Selection Panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Student"));
        selectionPanel.add(new JLabel("Student:"));
        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(300, 25));
        studentCombo.addActionListener(e -> {
            Student selected = (Student) studentCombo.getSelectedItem();
            if (selected != null) {
                currentStudentId = selected.getId();
                loadStudentCourses();
            }
        });
        selectionPanel.add(studentCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudentCourses());
        selectionPanel.add(refreshButton);

        // Courses Table
        String[] columns = {"Course Code", "Course Title", "Credit Hours",
                "CAT Score", "Exam Score", "Total", "Grade"};
        tableModel = new DefaultTableModel(columns, 0);
        coursesTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(coursesTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Enrolled Courses"));

        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Academic Summary"));
        summaryPanel.setPreferredSize(new Dimension(400, 80));

        JLabel avgLabel = new JLabel("Average Score:");
        JLabel avgValue = new JLabel("0.00");
        JLabel gradeLabel = new JLabel("Average Grade:");
        JLabel gradeValue = new JLabel("N/A");

        summaryPanel.add(avgLabel);
        summaryPanel.add(avgValue);
        summaryPanel.add(gradeLabel);
        summaryPanel.add(gradeValue);

        add(selectionPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void loadStudents() {
        studentCombo.removeAllItems();
        java.util.List<Student> students = studentController.getAllStudents();
        for (Student student : students) {
            studentCombo.addItem(student);
        }
    }

    private void loadStudentCourses() {
        tableModel.setRowCount(0);
        if (currentStudentId == null) return;

        Student student = studentController.getStudent(currentStudentId);
        if (student == null) return;

        double totalScore = 0;
        int courseCount = 0;

        for (Course course : student.getEnrolledCourses()) {
            Score score = scoreController.getStudentCourseScore(currentStudentId, course.getCourseCode());

            double catScore = 0;
            double examScore = 0;
            double total = 0;
            String grade = "N/A";

            if (score != null) {
                catScore = score.getCatScore();
                examScore = score.getExamScore();
                total = score.getTotal();
                grade = score.getGrade();
                totalScore += total;
                courseCount++;
            }

            tableModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getTitle(),
                    course.getCreditHours(),
                    catScore,
                    examScore,
                    total,
                    grade
            });
        }

        // Update summary
        JPanel summaryPanel = (JPanel) getComponent(2);
        JLabel avgValue = (JLabel) summaryPanel.getComponent(1);
        JLabel gradeValue = (JLabel) summaryPanel.getComponent(3);

        if (courseCount > 0) {
            double average = totalScore / courseCount;
            avgValue.setText(String.format("%.2f", average));
            gradeValue.setText(getGrade(average));
        } else {
            avgValue.setText("0.00");
            gradeValue.setText("N/A");
        }
    }

    private String getGrade(double score) {
        if (score >= 70) return "A";
        else if (score >= 60) return "B";
        else if (score >= 50) return "C";
        else if (score >= 40) return "D";
        else return "F";
    }
}