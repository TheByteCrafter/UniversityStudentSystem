package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import controller.StudentController;
import controller.CourseController;
import model.Student;
import model.Course;
import utilis.DuplicateEntryException;

public class StudentView extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private StudentController studentController;
    private CourseController courseController;
    private JTextField idField, nameField, emailField, phoneField, regNoField, programmeField;
    private JComboBox<Integer> yearCombo;
    private JComboBox<Course> courseCombo;
    private DefaultListModel<Course> enrolledCoursesModel;
    private JList<Course> enrolledCoursesList;
    private String currentStudentId;

    public StudentView() {
        studentController = new StudentController();
        courseController = new CourseController();
        initUI();
        loadStudents();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Panel - Student Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        idField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        phoneField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Registration No:"), gbc);
        regNoField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(regNoField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Programme:"), gbc);
        programmeField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(programmeField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Year of Study:"), gbc);
        yearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        gbc.gridx = 1;
        formPanel.add(yearCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Student");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> {
            try {
                addStudent();
            } catch (DuplicateEntryException ex) {

                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Duplicate Student Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this,
                        "Error adding student: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Center Panel - Student Table
        String[] columns = {"ID", "Name", "Registration No", "Programme", "Year", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedStudent();
            }
        });
        JScrollPane tableScroll = new JScrollPane(studentTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Students List"));

        // Right Panel - Course Enrollment
        JPanel enrollmentPanel = new JPanel(new BorderLayout());
        enrollmentPanel.setBorder(BorderFactory.createTitledBorder("Course Enrollment"));

        // Available courses
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        courseCombo = new JComboBox<>();
        loadAvailableCourses();
        JButton enrollButton = new JButton("Enroll in Course");
        enrollButton.addActionListener(e -> enrollStudent());
        availablePanel.add(courseCombo, BorderLayout.CENTER);
        availablePanel.add(enrollButton, BorderLayout.SOUTH);

        // Enrolled courses
        JPanel enrolledPanel = new JPanel(new BorderLayout());
        enrolledPanel.setBorder(BorderFactory.createTitledBorder("Enrolled Courses"));
        enrolledCoursesModel = new DefaultListModel<>();
        enrolledCoursesList = new JList<>(enrolledCoursesModel);
        JButton removeButton = new JButton("Remove Course");
        removeButton.addActionListener(e -> removeCourse());
        enrolledPanel.add(new JScrollPane(enrolledCoursesList), BorderLayout.CENTER);
        enrolledPanel.add(removeButton, BorderLayout.SOUTH);

        enrollmentPanel.add(availablePanel, BorderLayout.NORTH);
        enrollmentPanel.add(enrolledPanel, BorderLayout.CENTER);

        // Split pane
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, tableScroll);
        leftSplit.setResizeWeight(0.4);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, enrollmentPanel);
        mainSplit.setResizeWeight(0.7);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        List<Student> students = studentController.getAllStudents();
        for (Student student : students) {
            tableModel.addRow(new Object[]{
                    student.getId(),
                    student.getName(),
                    student.getRegistrationNumber(),
                    student.getProgramme(),
                    student.getYearOfStudy(),
                    student.getEmail(),
                    student.getPhone()
            });
        }
    }

    private void loadAvailableCourses() {
        courseCombo.removeAllItems();

        // Get all courses from database
        List<Course> allCourses = courseController.getAllCourses();

        if (currentStudentId != null) {
            // If a student is selected, show only courses they're not enrolled in
            Student student = studentController.getStudent(currentStudentId);
            if (student != null) {
                List<Course> availableCourses = courseController.getAvailableCoursesForStudent(
                        currentStudentId,
                        student.getEnrolledCourses()
                );

                if (availableCourses.isEmpty()) {
                    courseCombo.addItem(null);
                    courseCombo.setEnabled(false);
                    JOptionPane.showMessageDialog(this,
                            "No available courses to enroll!\nStudent is already enrolled in maximum courses.",
                            "No Courses Available",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    courseCombo.setEnabled(true);
                    for (Course course : availableCourses) {
                        courseCombo.addItem(course);
                    }
                }
            }
        } else {
            // Show all courses if no student selected
            if (allCourses.isEmpty()) {
                courseCombo.addItem(null);
                JOptionPane.showMessageDialog(this,
                        "No courses found in the system!\nPlease add courses first.",
                        "No Courses",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                for (Course course : allCourses) {
                    courseCombo.addItem(course);
                }
            }
        }
    }

    private void loadSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentStudentId = (String) tableModel.getValueAt(selectedRow, 0);
            Student student = studentController.getStudent(currentStudentId);
            if (student != null) {
                idField.setText(student.getId());
                nameField.setText(student.getName());
                emailField.setText(student.getEmail());
                phoneField.setText(student.getPhone());
                regNoField.setText(student.getRegistrationNumber());
                programmeField.setText(student.getProgramme());
                yearCombo.setSelectedItem(student.getYearOfStudy());

                // Load enrolled courses
                enrolledCoursesModel.clear();
                for (Course course : student.getEnrolledCourses()) {
                    enrolledCoursesModel.addElement(course);
                }

                // Refresh available courses based on enrolled courses
                loadAvailableCourses();
            }
        }
    }


    private void addStudent() throws DuplicateEntryException {
        if (validateForm()) {
            Student student = new Student(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    regNoField.getText(),
                    programmeField.getText(),
                    (Integer) yearCombo.getSelectedItem()
            );
            studentController.addStudent(student);
            loadStudents();
            clearForm();
            JOptionPane.showMessageDialog(this, "Student added successfully!");
        }
    }

    private void updateStudent() {
        if (currentStudentId != null && validateForm()) {
            Student student = new Student(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    regNoField.getText(),
                    programmeField.getText(),
                    (Integer) yearCombo.getSelectedItem()
            );
            studentController.updateStudent(student);
            loadStudents();
            JOptionPane.showMessageDialog(this, "Student updated successfully!");
        }
    }

    private void deleteStudent() {
        if (currentStudentId != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this student?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                studentController.deleteStudent(currentStudentId);
                loadStudents();
                clearForm();
                JOptionPane.showMessageDialog(this, "Student deleted successfully!");
            }
        }
    }

    private void enrollStudent() {
        if (currentStudentId != null) {
            Course selectedCourse = (Course) courseCombo.getSelectedItem();
            if (selectedCourse != null) {
                studentController.enrollStudentInCourse(currentStudentId, selectedCourse);
                loadSelectedStudent();
                JOptionPane.showMessageDialog(this, "Student enrolled in course successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student first!");
        }
    }

    private void removeCourse() {
        Course selectedCourse = enrolledCoursesList.getSelectedValue();
        if (selectedCourse != null && currentStudentId != null) {
            studentController.removeStudentFromCourse(currentStudentId, selectedCourse.getCourseCode());
            loadSelectedStudent();
            JOptionPane.showMessageDialog(this, "Course removed successfully!");
        }
    }

    private boolean validateForm() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID is required!");
            return false;
        }
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required!");
            return false;
        }
        return true;
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        regNoField.setText("");
        programmeField.setText("");
        yearCombo.setSelectedIndex(0);
        currentStudentId = null;
        enrolledCoursesModel.clear();
    }
}