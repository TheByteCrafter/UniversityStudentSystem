package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import controller.ScoreController;
import controller.StudentController;
import controller.CourseController;
import model.Score;
import model.Student;
import model.Course;

public class ScoreView extends JPanel {
    private JComboBox<Student> studentCombo;
    private JComboBox<Course> courseCombo;
    private JTextField catField, examField;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private ScoreController scoreController;
    private StudentController studentController;
    private CourseController courseController;

    // For live filtering
    private List<Student> allStudents;
    private List<Course> allCourses;
    private DefaultComboBoxModel<Student> studentModel;
    private DefaultComboBoxModel<Course> courseModel;

    public ScoreView() {
        scoreController = new ScoreController();
        studentController = new StudentController();
        courseController = new CourseController();

        // Initialize lists first
        allStudents = new ArrayList<>();
        allCourses = new ArrayList<>();

        initUI();
        loadAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Scores"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Student ComboBox with live search
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Student:"), gbc);
        studentModel = new DefaultComboBoxModel<>();
        studentCombo = new JComboBox<>(studentModel);
        studentCombo.setEditable(true);
        studentCombo.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        inputPanel.add(studentCombo, gbc);

        // Course ComboBox with live search
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Course:"), gbc);
        courseModel = new DefaultComboBoxModel<>();
        courseCombo = new JComboBox<>(courseModel);
        courseCombo.setEditable(true);
        courseCombo.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        inputPanel.add(courseCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("CAT Score (Max 30):"), gbc);
        catField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(catField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Exam Score (Max 70):"), gbc);
        examField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(examField, gbc);

        JButton saveButton = new JButton("Save Score");
        saveButton.addActionListener(e -> saveScore());
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(saveButton, gbc);

        // Search Panel with live search for student scores
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Student Scores"));
        JLabel searchLabel = new JLabel("Student:");
        JComboBox<Student> searchCombo = new JComboBox<>();
        searchCombo.setEditable(true);
        searchCombo.setPreferredSize(new Dimension(250, 25));

        // Setup live search for search combo (after allStudents is populated)
        setupLiveSearchForSearch(searchCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadAllData();
            JOptionPane.showMessageDialog(this, "Data refreshed from database!");
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchCombo);
        searchPanel.add(refreshButton);

        // Table Panel
        String[] columns = {"Student ID", "Student Name", "Course Code", "Course Title",
                "CAT (30)", "Exam (70)", "Total", "Grade"};
        tableModel = new DefaultTableModel(columns, 0);
        scoreTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(scoreTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Scores List"));

        // Add components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);

        // Setup live search for student and course combos after components are created
        setupLiveSearch(studentCombo, true);
        setupLiveSearch(courseCombo, false);
    }

    private void setupLiveSearch(JComboBox<?> comboBox, boolean isStudent) {
        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = editor.getText().toLowerCase();
                if (isStudent) {
                    filterStudents(text);
                } else {
                    filterCourses(text);
                }
                comboBox.showPopup();
            }
        });
    }

    private void setupLiveSearchForSearch(JComboBox<Student> searchCombo) {
        // Populate with all students (if available)
        DefaultComboBoxModel<Student> searchModel = new DefaultComboBoxModel<>();
        if (allStudents != null) {
            for (Student student : allStudents) {
                searchModel.addElement(student);
            }
        }
        searchCombo.setModel(searchModel);

        JTextField editor = (JTextField) searchCombo.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = editor.getText().toLowerCase();
                filterSearchStudents(searchCombo, text);
                searchCombo.showPopup();
            }
        });

        // Add action listener to load scores when student is selected
        searchCombo.addActionListener(e -> {
            Object selected = searchCombo.getSelectedItem();
            if (selected instanceof Student) {
                Student student = (Student) selected;
                loadStudentScores(student.getId());
            } else if (selected instanceof String) {
                String text = (String) selected;
                // Try to find student by ID or name
                if (allStudents != null) {
                    for (Student student : allStudents) {
                        if (student.getId().equalsIgnoreCase(text) ||
                                student.getName().toLowerCase().contains(text.toLowerCase())) {
                            loadStudentScores(student.getId());
                            break;
                        }
                    }
                }
            }
        });
    }

    private void filterStudents(String text) {
        studentModel.removeAllElements();
        if (allStudents == null || allStudents.isEmpty()) {
            return;
        }

        if (text.isEmpty()) {
            for (Student student : allStudents) {
                studentModel.addElement(student);
            }
        } else {
            List<Student> filtered = allStudents.stream()
                    .filter(s -> s.getId().toLowerCase().contains(text) ||
                            s.getName().toLowerCase().contains(text) ||
                            s.getRegistrationNumber().toLowerCase().contains(text))
                    .collect(Collectors.toList());
            for (Student student : filtered) {
                studentModel.addElement(student);
            }
        }

        // If exactly one match, select it
        if (studentModel.getSize() == 1) {
            studentCombo.setSelectedIndex(0);
        }
    }

    private void filterCourses(String text) {
        courseModel.removeAllElements();
        if (allCourses == null || allCourses.isEmpty()) {
            return;
        }

        if (text.isEmpty()) {
            for (Course course : allCourses) {
                courseModel.addElement(course);
            }
        } else {
            List<Course> filtered = allCourses.stream()
                    .filter(c -> c.getCourseCode().toLowerCase().contains(text) ||
                            c.getTitle().toLowerCase().contains(text))
                    .collect(Collectors.toList());
            for (Course course : filtered) {
                courseModel.addElement(course);
            }
        }

        // If exactly one match, select it
        if (courseModel.getSize() == 1) {
            courseCombo.setSelectedIndex(0);
        }
    }

    private void filterSearchStudents(JComboBox<Student> searchCombo, String text) {
        DefaultComboBoxModel<Student> searchModel = new DefaultComboBoxModel<>();

        if (allStudents == null || allStudents.isEmpty()) {
            searchCombo.setModel(searchModel);
            return;
        }

        if (text.isEmpty()) {
            for (Student student : allStudents) {
                searchModel.addElement(student);
            }
        } else {
            List<Student> filtered = allStudents.stream()
                    .filter(s -> s.getId().toLowerCase().contains(text) ||
                            s.getName().toLowerCase().contains(text) ||
                            s.getRegistrationNumber().toLowerCase().contains(text))
                    .collect(Collectors.toList());
            for (Student student : filtered) {
                searchModel.addElement(student);
            }
        }
        searchCombo.setModel(searchModel);
    }

    private void loadAllData() {
        // Refresh data from controllers
        studentController.refreshFromDatabase();
        courseController.refreshFromDatabase();
        scoreController.refreshFromDatabase();

        // Load all students and courses
        allStudents = studentController.getAllStudents();
        allCourses = courseController.getAllCourses();

        // Update combo boxes
        filterStudents("");
        filterCourses("");

        if (allStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No students found. Please add students first.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No courses found. Please add courses first.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadStudentScores(String studentId) {
        tableModel.setRowCount(0);
        List<Score> scores = scoreController.getStudentScores(studentId);
        Student student = studentController.getStudent(studentId);

        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No scores found for this student.",
                    "No Scores",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Score score : scores) {
            Course course = courseController.getCourse(score.getCourseCode());
            tableModel.addRow(new Object[]{
                    studentId,
                    student != null ? student.getName() : "Unknown",
                    score.getCourseCode(),
                    course != null ? course.getTitle() : "Unknown",
                    score.getCatScore(),
                    score.getExamScore(),
                    score.getTotal(),
                    score.getGrade()
            });
        }
    }

    private void saveScore() {
        // Get selected student from combo box
        Object selectedStudentObj = studentCombo.getSelectedItem();
        Student selectedStudent = null;

        if (selectedStudentObj instanceof Student) {
            selectedStudent = (Student) selectedStudentObj;
        } else if (selectedStudentObj instanceof String) {
            String text = (String) selectedStudentObj;
            // Try to find student by ID or name
            if (allStudents != null) {
                for (Student student : allStudents) {
                    if (student.getId().equalsIgnoreCase(text) ||
                            student.getName().equalsIgnoreCase(text)) {
                        selectedStudent = student;
                        break;
                    }
                }
            }
        }

        // Get selected course from combo box
        Object selectedCourseObj = courseCombo.getSelectedItem();
        Course selectedCourse = null;

        if (selectedCourseObj instanceof Course) {
            selectedCourse = (Course) selectedCourseObj;
        } else if (selectedCourseObj instanceof String) {
            String text = (String) selectedCourseObj;
            // Try to find course by code or title
            if (allCourses != null) {
                for (Course course : allCourses) {
                    if (course.getCourseCode().equalsIgnoreCase(text) ||
                            course.getTitle().equalsIgnoreCase(text)) {
                        selectedCourse = course;
                        break;
                    }
                }
            }
        }

        if (selectedStudent == null || selectedCourse == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid student and course!\n" +
                            "You can type to search or select from the dropdown.",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double catScore = Double.parseDouble(catField.getText());
            double examScore = Double.parseDouble(examField.getText());

            if (catScore < 0 || catScore > 30) {
                JOptionPane.showMessageDialog(this, "CAT score must be between 0 and 30!");
                return;
            }
            if (examScore < 0 || examScore > 70) {
                JOptionPane.showMessageDialog(this, "Exam score must be between 0 and 70!");
                return;
            }

            // Check if score already exists for this student and course
            Score existingScore = scoreController.getStudentCourseScore(
                    selectedStudent.getId(),
                    selectedCourse.getCourseCode()
            );

            if (existingScore != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Score already exists for this student and course.\n" +
                                "CAT: " + existingScore.getCatScore() + ", Exam: " + existingScore.getExamScore() + "\n" +
                                "Do you want to update it?",
                        "Score Exists",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                // Update existing score
                Score updatedScore = new Score(catScore, examScore,
                        selectedCourse.getCourseCode(),
                        selectedStudent.getId());
                scoreController.updateScore(updatedScore);
                JOptionPane.showMessageDialog(this, "Score updated successfully!");
            } else {
                // Add new score
                Score score = new Score(catScore, examScore,
                        selectedCourse.getCourseCode(),
                        selectedStudent.getId());
                scoreController.addScore(score);
                JOptionPane.showMessageDialog(this, "Score saved successfully!");
            }

            // Clear fields
            catField.setText("");
            examField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for scores!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving score: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}