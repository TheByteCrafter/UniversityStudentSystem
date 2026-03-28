package views;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import controller.StudentController;
import controller.ScoreController;
import controller.CourseController;
import model.Student;
import model.Course;
import model.Score;
import java.util.List;

public class ResultSlipView extends JPanel {
    private JComboBox<Student> studentCombo;
    private JTextArea resultArea;
    private StudentController studentController;
    private ScoreController scoreController;
    private CourseController courseController;
    private String currentStudentId;

    public ResultSlipView() {
        studentController = new StudentController();
        scoreController = new ScoreController();
        courseController = new CourseController();
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
                generateResultSlip();
            }
        });
        selectionPanel.add(studentCombo);

        JButton printButton = new JButton("Print Result Slip");
        printButton.addActionListener(e -> printResultSlip());
        selectionPanel.add(printButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> generateResultSlip());
        selectionPanel.add(refreshButton);

        // Result Display Area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Result Slip"));

        add(selectionPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadStudents() {
        studentCombo.removeAllItems();
        List<Student> students = studentController.getAllStudents();
        for (Student student : students) {
            studentCombo.addItem(student);
        }
    }

    private void generateResultSlip() {
        if (currentStudentId == null) return;

        Student student = studentController.getStudent(currentStudentId);
        if (student == null) return;

        StringBuilder result = new StringBuilder();

        // Header
        result.append("=".repeat(80)).append("\n");
        result.append(String.format("%80s\n", "CHUKA UNIVERSITY"));
        result.append(String.format("%80s\n", "STUDENT RESULT SLIP"));
        result.append("=".repeat(80)).append("\n\n");

        // Student Information
        result.append("Student Name: ").append(student.getName()).append("\n");
        result.append("Registration No: ").append(student.getRegistrationNumber()).append("\n");
        result.append("Programme: ").append(student.getProgramme()).append("\n");
        result.append("Year of Study: ").append(student.getYearOfStudy()).append("\n");
        result.append("\n");

        // Results Table
        result.append("-".repeat(80)).append("\n");
        result.append(String.format("%-15s %-40s %-10s %-10s %-10s\n",
                "Course Code", "Course Title", "Score", "Grade", "Status"));
        result.append("-".repeat(80)).append("\n");

        double totalScore = 0;
        int courseCount = 0;
        int passedCourses = 0;

        for (Course course : student.getEnrolledCourses()) {
            Score score = scoreController.getStudentCourseScore(currentStudentId, course.getCourseCode());

            String scoreStr = "N/A";
            String grade = "N/A";
            String status = "Not Taken";

            if (score != null) {
                double total = score.getTotal();
                scoreStr = String.format("%.2f", total);
                grade = score.getGrade();
                status = total >= 40 ? "PASS" : "FAIL";
                totalScore += total;
                courseCount++;
                if (total >= 40) passedCourses++;
            }

            result.append(String.format("%-15s %-40s %-10s %-10s %-10s\n",
                    course.getCourseCode(),
                    course.getTitle().length() > 40 ?
                            course.getTitle().substring(0, 37) + "..." :
                            course.getTitle(),
                    scoreStr, grade, status));
        }

        result.append("-".repeat(80)).append("\n");

        // Summary
        if (courseCount > 0) {
            double average = totalScore / courseCount;
            String avgGrade = getGrade(average);
            double percentage = (passedCourses * 100.0) / courseCount;

            result.append(String.format("\nTotal Courses: %d\n", courseCount));
            result.append(String.format("Courses Passed: %d\n", passedCourses));
            result.append(String.format("Courses Failed: %d\n", courseCount - passedCourses));
            result.append(String.format("Pass Percentage: %.2f%%\n", percentage));
            result.append(String.format("\nAverage Score: %.2f\n", average));
            result.append(String.format("Average Grade: %s\n", avgGrade));

            // Overall Status
            result.append("\nOverall Status: ");
            if (average >= 70) {
                result.append("FIRST CLASS HONOURS");
            } else if (average >= 60) {
                result.append("SECOND CLASS HONOURS (UPPER DIVISION)");
            } else if (average >= 50) {
                result.append("SECOND CLASS HONOURS (LOWER DIVISION)");
            } else if (average >= 40) {
                result.append("PASS");
            } else {
                result.append("FAIL - PROBATION");
            }
            result.append("\n");
        } else {
            result.append("\nNo courses enrolled or no scores available.\n");
        }

        result.append("\n");
        result.append("=".repeat(80)).append("\n");
        result.append(String.format("%80s\n", "END OF RESULT SLIP"));
        result.append(String.format("%80s\n", "Generated on: " + new java.util.Date()));
        result.append("=".repeat(80)).append("\n");

        resultArea.setText(result.toString());
    }

    private void printResultSlip() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Draw the result area content
                    Font font = new Font("Monospaced", Font.PLAIN, 10);
                    g2d.setFont(font);

                    String[] lines = resultArea.getText().split("\n");
                    int y = 20;
                    int lineHeight = g2d.getFontMetrics().getHeight();

                    for (String line : lines) {
                        g2d.drawString(line, 20, y);
                        y += lineHeight;

                        // Check for page overflow (simple implementation)
                        if (y > pageFormat.getImageableHeight() - 20) {
                            return PAGE_EXISTS;
                        }
                    }

                    return PAGE_EXISTS;
                }
            });

            boolean doPrint = job.printDialog();
            if (doPrint) {
                job.print();
                JOptionPane.showMessageDialog(this, "Result slip sent to printer!",
                        "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
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