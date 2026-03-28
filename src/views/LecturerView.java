package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import controller.LecturerController;
import controller.CourseController;
import model.Lecturer;
import model.Course;
import utilis.DuplicateEntryException;

public class LecturerView extends JPanel {
    private JTable lecturerTable;
    private DefaultTableModel tableModel;
    private LecturerController lecturerController;
    private CourseController courseController;
    private JTextField idField, nameField, emailField, phoneField, staffNoField, deptField;
    private JComboBox<Course> courseCombo;
    private DefaultListModel<Course> assignedCoursesModel;
    private JList<Course> assignedCoursesList;
    private String currentLecturerId;
    private JButton refreshButton;

    public LecturerView() {
        lecturerController = new LecturerController();
        courseController = new CourseController();
        initUI();
        loadLecturers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Lecturer Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        formPanel.add(new JLabel("Staff Number:"), gbc);
        staffNoField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(staffNoField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Department:"), gbc);
        deptField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(deptField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Lecturer");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> {
            try {
                addLecturer();
            } catch (DuplicateEntryException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Duplicate Entry Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error adding lecturer: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        updateButton.addActionListener(e -> updateLecturer());
        deleteButton.addActionListener(e -> deleteLecturer());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> refreshAllData());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Table Panel
        String[] columns = {"ID", "Name", "Staff Number", "Department", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0);
        lecturerTable = new JTable(tableModel);
        lecturerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lecturerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedLecturer();
            }
        });
        JScrollPane tableScroll = new JScrollPane(lecturerTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Lecturers List"));

        // Course Assignment Panel
        JPanel assignmentPanel = new JPanel(new BorderLayout());
        assignmentPanel.setBorder(BorderFactory.createTitledBorder("Course Assignment"));

        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        courseCombo = new JComboBox<>();
        setupCourseComboRenderer(); // Add custom renderer for combo box
        JButton assignButton = new JButton("Assign Course");
        assignButton.addActionListener(e -> assignCourse());
        availablePanel.add(courseCombo, BorderLayout.CENTER);
        availablePanel.add(assignButton, BorderLayout.SOUTH);

        JPanel assignedPanel = new JPanel(new BorderLayout());
        assignedPanel.setBorder(BorderFactory.createTitledBorder("Assigned Courses"));
        assignedCoursesModel = new DefaultListModel<>();
        assignedCoursesList = new JList<>(assignedCoursesModel);
        setupCourseListRenderer(); // Add custom renderer for list
        JButton removeButton = new JButton("Remove Assignment");
        removeButton.addActionListener(e -> removeAssignment());
        assignedPanel.add(new JScrollPane(assignedCoursesList), BorderLayout.CENTER);
        assignedPanel.add(removeButton, BorderLayout.SOUTH);

        assignmentPanel.add(availablePanel, BorderLayout.NORTH);
        assignmentPanel.add(assignedPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, assignmentPanel);
        splitPane.setResizeWeight(0.5);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, tableScroll);
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void setupCourseComboRenderer() {
        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course course = (Course) value;
                    setText(course.getCourseCode() + " - " + course.getTitle());
                } else if (value == null) {
                    setText("No courses available");
                }
                return this;
            }
        });
    }

    private void setupCourseListRenderer() {
        assignedCoursesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course course = (Course) value;
                    setText(course.getCourseCode() + " - " + course.getTitle());
                }
                return this;
            }
        });
    }

    private void refreshAllData() {
        // Refresh controllers from database
        lecturerController.refreshFromDatabase();
        courseController.refreshFromDatabase();

        // Refresh UI
        loadLecturers();
        if (currentLecturerId != null) {
            loadSelectedLecturer();
        } else {
            loadAvailableCourses();
        }

        JOptionPane.showMessageDialog(this,
                "Data refreshed from database!",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadLecturers() {
        tableModel.setRowCount(0);
        List<Lecturer> lecturers = lecturerController.getAllLecturers();
        for (Lecturer lecturer : lecturers) {
            tableModel.addRow(new Object[]{
                    lecturer.getId(),
                    lecturer.getName(),
                    lecturer.getStaffNumber(),
                    lecturer.getDepartment(),
                    lecturer.getEmail(),
                    lecturer.getPhone()
            });
        }
    }

    private void loadAvailableCourses() {
        courseCombo.removeAllItems();

        // Get all unassigned courses or courses assigned to other lecturers
        List<Course> allCourses = courseController.getAllCourses();

        if (allCourses.isEmpty()) {
            courseCombo.addItem(null);
            courseCombo.setEnabled(false);
            return;
        }

        if (currentLecturerId != null) {
            // Show courses that are not assigned to this lecturer
            Lecturer lecturer = lecturerController.getLecturer(currentLecturerId);
            if (lecturer != null) {
                boolean hasAvailable = false;
                for (Course course : allCourses) {
                    boolean isAssigned = false;
                    for (Course assigned : lecturer.getAssignedCourses()) {
                        if (assigned.getCourseCode().equals(course.getCourseCode())) {
                            isAssigned = true;
                            break;
                        }
                    }
                    // Also show courses assigned to other lecturers?
                    // For now, show all courses not assigned to this lecturer
                    if (!isAssigned) {
                        courseCombo.addItem(course);
                        hasAvailable = true;
                    }
                }

                if (!hasAvailable) {
                    courseCombo.addItem(null);
                    courseCombo.setEnabled(false);
                } else {
                    courseCombo.setEnabled(true);
                }
            }
        } else {
            // No lecturer selected, show all courses but disable assignment
            for (Course course : allCourses) {
                courseCombo.addItem(course);
            }
            courseCombo.setEnabled(false);
        }
    }

    private void loadSelectedLecturer() {
        int selectedRow = lecturerTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentLecturerId = (String) tableModel.getValueAt(selectedRow, 0);
            Lecturer lecturer = lecturerController.getLecturer(currentLecturerId);
            if (lecturer != null) {
                idField.setText(lecturer.getId());
                nameField.setText(lecturer.getName());
                emailField.setText(lecturer.getEmail());
                phoneField.setText(lecturer.getPhone());
                staffNoField.setText(lecturer.getStaffNumber());
                deptField.setText(lecturer.getDepartment());

                // Load assigned courses
                assignedCoursesModel.clear();
                List<Course> assignedCourses = lecturer.getAssignedCourses();
                if (assignedCourses.isEmpty()) {
                    assignedCoursesModel.addElement(null);
                } else {
                    for (Course course : assignedCourses) {
                        assignedCoursesModel.addElement(course);
                    }
                }

                // Refresh available courses based on assigned courses
                loadAvailableCourses();
            }
        } else {
            currentLecturerId = null;
            clearForm();
        }
    }

    private void addLecturer() throws DuplicateEntryException {
        if (validateForm()) {
            // Check if lecturer ID already exists
            if (lecturerController.getLecturer(idField.getText()) != null) {
                throw new DuplicateEntryException("Lecturer ID already exists!", "id", idField.getText());
            }

            Lecturer lecturer = new Lecturer(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    staffNoField.getText(),
                    deptField.getText()
            );
            lecturerController.addLecturer(lecturer);
            loadLecturers();
            clearForm();
            JOptionPane.showMessageDialog(this, "Lecturer added successfully!");
        }
    }

    private void updateLecturer() {
        if (currentLecturerId != null && validateForm()) {
            Lecturer lecturer = new Lecturer(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    staffNoField.getText(),
                    deptField.getText()
            );
            lecturerController.updateLecturer(lecturer);
            loadLecturers();
            JOptionPane.showMessageDialog(this, "Lecturer updated successfully!");
        }
    }

    private void deleteLecturer() {
        if (currentLecturerId != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this lecturer?\nThis will unassign all their courses!",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                lecturerController.deleteLecturer(currentLecturerId);
                loadLecturers();
                clearForm();
                JOptionPane.showMessageDialog(this, "Lecturer deleted successfully!");
            }
        }
    }

    private void assignCourse() {
        if (currentLecturerId != null) {
            Course selectedCourse = (Course) courseCombo.getSelectedItem();
            if (selectedCourse != null) {
                Lecturer lecturer = lecturerController.getLecturer(currentLecturerId);
                if (lecturer != null) {
                    // Check if already assigned
                    for (Course assigned : lecturer.getAssignedCourses()) {
                        if (assigned.getCourseCode().equals(selectedCourse.getCourseCode())) {
                            JOptionPane.showMessageDialog(this,
                                    "Course is already assigned to this lecturer!",
                                    "Duplicate Assignment",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    lecturerController.assignCourseToLecturer(currentLecturerId, selectedCourse);
                    loadSelectedLecturer();
                    JOptionPane.showMessageDialog(this,
                            "Course assigned successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a course to assign!",
                        "No Course Selected",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a lecturer first!",
                    "No Lecturer Selected",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeAssignment() {
        Course selectedCourse = assignedCoursesList.getSelectedValue();
        if (selectedCourse != null && currentLecturerId != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove " + selectedCourse.getCourseCode() + " from this lecturer?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                lecturerController.removeCourseFromLecturer(currentLecturerId, selectedCourse.getCourseCode());
                loadSelectedLecturer();
                JOptionPane.showMessageDialog(this, "Course assignment removed successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to remove!",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean validateForm() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lecturer ID is required!");
            return false;
        }
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lecturer name is required!");
            return false;
        }
        if (staffNoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Staff number is required!");
            return false;
        }
        return true;
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        staffNoField.setText("");
        deptField.setText("");
        currentLecturerId = null;
        assignedCoursesModel.clear();
        courseCombo.removeAllItems();
        courseCombo.setEnabled(false);
        idField.requestFocus();
    }
}