package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import controller.CourseController;
import controller.LecturerController;
import model.Course;
import model.Lecturer;
import utilis.DuplicateEntryException;

public class CourseView extends JPanel {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private CourseController courseController;
    private LecturerController lecturerController;
    private JTextField codeField, titleField, hoursField;
    private JComboBox<Lecturer> lecturerCombo;
    private String currentCourseCode;

    public CourseView() {
        courseController = new CourseController();
        lecturerController = new LecturerController();
        initUI();
        loadCourses();
        loadLecturers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Course Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Course Code:"), gbc);
        codeField = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Title:"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Credit Hours:"), gbc);
        hoursField = new JTextField(5);
        gbc.gridx = 1;
        formPanel.add(hoursField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Lecturer:"), gbc);
        lecturerCombo = new JComboBox<>();
        lecturerCombo.addItem(null); // Add null option for no lecturer
        gbc.gridx = 1;
        formPanel.add(lecturerCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Course");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> {
            try {
                addCourse();
            } catch (DuplicateEntryException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Duplicate Entry Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error adding course: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        updateButton.addActionListener(e -> {
            try {
                updateCourse();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error updating course: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            try {
                deleteCourse();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting course: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        clearButton.addActionListener(e -> clearForm());

        refreshButton.addActionListener(e -> {
            loadCourses();
            loadLecturers();
            JOptionPane.showMessageDialog(this,
                    "Courses refreshed from database!",
                    "Refreshed",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Table Panel
        String[] columns = {"Course Code", "Title", "Credit Hours", "Lecturer"};
        tableModel = new DefaultTableModel(columns, 0);
        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedCourse();
            }
        });
        JScrollPane tableScroll = new JScrollPane(courseTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Courses List"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, tableScroll);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses = courseController.getAllCourses();
        for (Course course : courses) {
            String lecturerName = "";
            if (course.getLecturerId() != null) {
                Lecturer lecturer = lecturerController.getLecturer(course.getLecturerId());
                if (lecturer != null) {
                    lecturerName = lecturer.getName();
                }
            }
            tableModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getTitle(),
                    course.getCreditHours(),
                    lecturerName
            });
        }

        // Show message if no courses
        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No courses found in the system.\nPlease add courses using the form above.",
                    "No Courses",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadLecturers() {
        lecturerCombo.removeAllItems();
        lecturerCombo.addItem(null); // Option for no lecturer

        List<Lecturer> lecturers = lecturerController.getAllLecturers();
        for (Lecturer lecturer : lecturers) {
            lecturerCombo.addItem(lecturer);
        }
    }

    private void loadSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentCourseCode = (String) tableModel.getValueAt(selectedRow, 0);
            Course course = courseController.getCourse(currentCourseCode);
            if (course != null) {
                codeField.setText(course.getCourseCode());
                codeField.setEditable(false); // Don't allow editing course code
                titleField.setText(course.getTitle());
                hoursField.setText(String.valueOf(course.getCreditHours()));

                // Select the lecturer
                boolean found = false;
                for (int i = 0; i < lecturerCombo.getItemCount(); i++) {
                    Lecturer lecturer = lecturerCombo.getItemAt(i);
                    if (lecturer != null && lecturer.getId().equals(course.getLecturerId())) {
                        lecturerCombo.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    lecturerCombo.setSelectedIndex(0); // Select null option
                }
            }
        }
    }

    private void addCourse() throws DuplicateEntryException {
        if (validateForm()) {
            Lecturer selectedLecturer = (Lecturer) lecturerCombo.getSelectedItem();
            Course course = new Course(
                    codeField.getText().toUpperCase(),
                    titleField.getText(),
                    Integer.parseInt(hoursField.getText()),
                    selectedLecturer != null ? selectedLecturer.getId() : null
            );
            courseController.addCourse(course);
            loadCourses();
            clearForm();
            JOptionPane.showMessageDialog(this, "Course added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateCourse() {
        if (currentCourseCode != null && validateForm()) {
            Lecturer selectedLecturer = (Lecturer) lecturerCombo.getSelectedItem();
            Course course = new Course(
                    codeField.getText().toUpperCase(),
                    titleField.getText(),
                    Integer.parseInt(hoursField.getText()),
                    selectedLecturer != null ? selectedLecturer.getId() : null
            );
            courseController.updateCourse(course);
            loadCourses();
            JOptionPane.showMessageDialog(this, "Course updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteCourse() {
        if (currentCourseCode != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this course?\n" +
                            "This will also remove it from all enrolled students!",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                courseController.deleteCourse(currentCourseCode);
                loadCourses();
                clearForm();
                JOptionPane.showMessageDialog(this, "Course deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validateForm() {
        if (codeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course code is required!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course title is required!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (hoursField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Credit hours is required!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int hours = Integer.parseInt(hoursField.getText());
            if (hours <= 0 || hours > 6) {
                JOptionPane.showMessageDialog(this, "Credit hours must be between 1 and 6!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credit hours must be a number!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        codeField.setText("");
        codeField.setEditable(true);
        titleField.setText("");
        hoursField.setText("");
        if (lecturerCombo.getItemCount() > 0) {
            lecturerCombo.setSelectedIndex(0);
        }
        currentCourseCode = null;
        codeField.requestFocus();
    }
}