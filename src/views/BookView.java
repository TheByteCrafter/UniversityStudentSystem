package views;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import controller.BookController;
import model.Book;
import model.Student;

public class BookView extends JPanel {
    private JTextField searchField;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private BookController bookController;
    private JTextArea availabilityArea;
    private JList<String> reservedBooksList;
    private DefaultListModel<String> reservedBooksModel;
    private JComboBox<String> reserveStudentCombo;

    public BookView() {
        bookController = new BookController();
        initUI();
        loadAllBooks();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Panel - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Books"));
        searchPanel.add(new JLabel("Search by Title:"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { searchBooks(); }
            @Override
            public void removeUpdate(DocumentEvent e) { searchBooks(); }
            @Override
            public void changedUpdate(DocumentEvent e) { searchBooks(); }
        });
        searchPanel.add(searchField);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadAllBooks();
            loadReservations();
        });
        searchPanel.add(refreshButton);

        add(searchPanel, BorderLayout.NORTH);

        // Center Panel - Split Pane
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left Panel - Book Table
        JPanel leftPanel = new JPanel(new BorderLayout());
        String[] columns = {"ISBN", "Title", "Edition", "Version", "Year", "Total Copies", "Available"};
        tableModel = new DefaultTableModel(columns, 0);
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showBookAvailability();
            }
        });
        leftPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        centerSplit.setLeftComponent(leftPanel);

        // Right Panel - Book Actions and Reservations
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Availability Panel
        JPanel availabilityPanel = new JPanel(new BorderLayout());
        availabilityPanel.setBorder(BorderFactory.createTitledBorder("Book Availability"));
        availabilityArea = new JTextArea(5, 20);
        availabilityArea.setEditable(false);
        availabilityArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        availabilityPanel.add(new JScrollPane(availabilityArea), BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Book Actions"));

        JButton borrowButton = new JButton("Borrow Book");
        JButton returnButton = new JButton("Return Book");
        JButton reserveButton = new JButton("Reserve Book");
        JButton viewReservationsButton = new JButton("View My Reservations");

        borrowButton.addActionListener(e -> borrowBook());
        returnButton.addActionListener(e -> returnBook());
        reserveButton.addActionListener(e -> reserveBook());
        viewReservationsButton.addActionListener(e -> loadStudentReservations());

        actionPanel.add(borrowButton);
        actionPanel.add(returnButton);
        actionPanel.add(reserveButton);
        actionPanel.add(viewReservationsButton);

        // Reservations Panel
        JPanel reservationsPanel = new JPanel(new BorderLayout());
        reservationsPanel.setBorder(BorderFactory.createTitledBorder("Reserved Books"));

        JPanel reservationControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reservationControl.add(new JLabel("Student ID:"));
        reserveStudentCombo = new JComboBox<>();
        reserveStudentCombo.setEditable(true);
        reserveStudentCombo.setPreferredSize(new Dimension(150, 25));
        reservationControl.add(reserveStudentCombo);

        JButton loadReservationsButton = new JButton("Load Reservations");
        loadReservationsButton.addActionListener(e -> loadStudentReservations());
        reservationControl.add(loadReservationsButton);

        reservedBooksModel = new DefaultListModel<>();
        reservedBooksList = new JList<>(reservedBooksModel);
        reservedBooksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton cancelReservationButton = new JButton("Cancel Selected Reservation");
        cancelReservationButton.addActionListener(e -> cancelReservation());

        reservationsPanel.add(reservationControl, BorderLayout.NORTH);
        reservationsPanel.add(new JScrollPane(reservedBooksList), BorderLayout.CENTER);
        reservationsPanel.add(cancelReservationButton, BorderLayout.SOUTH);

        // Combine panels in right panel
        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.add(availabilityPanel, BorderLayout.NORTH);
        topRightPanel.add(actionPanel, BorderLayout.SOUTH);

        rightPanel.add(topRightPanel, BorderLayout.NORTH);
        rightPanel.add(reservationsPanel, BorderLayout.CENTER);

        centerSplit.setRightComponent(rightPanel);
        centerSplit.setResizeWeight(0.6);

        add(centerSplit, BorderLayout.CENTER);

        // Load students into combo
        loadStudentsForReservation();
    }

    private void searchBooks() {
        String searchTerm = searchField.getText();
        if (searchTerm.isEmpty()) {
            loadAllBooks();
        } else {
            List<Book> results = bookController.searchBooksByTitle(searchTerm);
            updateTable(results);
        }
    }

    private void loadAllBooks() {
        List<Book> allBooks = bookController.searchBooksByTitle("");
        updateTable(allBooks);
    }




    private void loadReservations() {
        // Refresh reservations for currently selected student
        loadStudentReservations();
    }

    private void updateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book book : books) {
            int availableCopies = book.getTotalCopies() - book.getBorrowedCopies();
            tableModel.addRow(new Object[]{
                    book.getIsbn(),
                    book.getTitle(),
                    book.getEdition(),
                    book.getVersion(),
                    book.getYearPublished(),
                    book.getTotalCopies(),
                    availableCopies
            });
        }
    }

    private void showBookAvailability() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);
            BookController.BookAvailability availability =
                    bookController.getBookAvailability(isbn);

            if (availability != null) {
                String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
                availabilityArea.setText(String.format(
                        "Book: %s\nISBN: %s\n\n" +
                                "Total Copies: %d\n" +
                                "Available Copies: %d\n" +
                                "Borrowed Copies: %d\n" +
                                "Reserved Copies: %d\n" +
                                "Overdue Copies: %d\n\n" +
                                "Status: %s",
                        bookTitle,
                        isbn,
                        availability.getTotalCopies(),
                        availability.getAvailableCopies(),
                        availability.getBorrowedCopies(),
                        availability.getReservedCopies(),
                        availability.getOverdueCopies(),
                        availability.getAvailableCopies() > 0 ? "AVAILABLE" : "NOT AVAILABLE"
                ));
            }
        }
    }

    private void borrowBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);

            // Create a combo box with student registration numbers
            JComboBox<String> studentCombo = new JComboBox<>();
            List<Student> students = bookController.getAllStudents();

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students found in the system!\nPlease add students first.",
                        "No Students",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Student student : students) {
                studentCombo.addItem(student.getRegistrationNumber() + " - " + student.getName());
            }
            studentCombo.setEditable(true);

            int result = JOptionPane.showConfirmDialog(this, studentCombo,
                    "Enter Student Registration Number:", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String selected = (String) studentCombo.getSelectedItem();
                String registrationNumber;

                if (selected.contains(" - ")) {
                    registrationNumber = selected.split(" - ")[0];
                } else {
                    registrationNumber = selected;
                }

                if (registrationNumber != null && !registrationNumber.trim().isEmpty()) {
                    String borrowResult = bookController.borrowBook(isbn, registrationNumber.trim());
                    if (borrowResult.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(this,
                                "Book borrowed successfully!\nDue date: 14 days from now",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadAllBooks();
                        showBookAvailability();
                        loadStudentReservations();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                borrowResult,
                                "Cannot Borrow",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a book first!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);

            // Create a combo box with student registration numbers
            JComboBox<String> studentCombo = new JComboBox<>();
            List<Student> students = bookController.getAllStudents();

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students found in the system!",
                        "No Students",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Student student : students) {
                studentCombo.addItem(student.getRegistrationNumber() + " - " + student.getName());
            }
            studentCombo.setEditable(true);

            int result = JOptionPane.showConfirmDialog(this, studentCombo,
                    "Enter Student Registration Number:", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String selected = (String) studentCombo.getSelectedItem();
                String registrationNumber;

                if (selected.contains(" - ")) {
                    registrationNumber = selected.split(" - ")[0];
                } else {
                    registrationNumber = selected;
                }

                if (registrationNumber != null && !registrationNumber.trim().isEmpty()) {
                    String returnResult = bookController.returnBook(isbn, registrationNumber.trim());
                    if (returnResult.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(this,
                                "Book returned successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadAllBooks();
                        showBookAvailability();
                        loadStudentReservations();

                        // Check if there are reservations for this book
                        if (bookController.hasReservationsForBook(isbn)) {
                            int notify = JOptionPane.showConfirmDialog(this,
                                    "This book has pending reservations.\n" +
                                            "Would you like to notify the next person in queue?",
                                    "Reservation Waiting",
                                    JOptionPane.YES_NO_OPTION);
                            if (notify == JOptionPane.YES_OPTION) {
                                bookController.notifyNextReservation(isbn);
                                JOptionPane.showMessageDialog(this,
                                        "Next person in queue has been notified!",
                                        "Notification Sent",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                                returnResult,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a book first!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reserveBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            String isbn = (String) tableModel.getValueAt(selectedRow, 0);

            // Create a combo box with student registration numbers
            JComboBox<String> studentCombo = new JComboBox<>();
            List<Student> students = bookController.getAllStudents();

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students found in the system!\nPlease add students first.",
                        "No Students",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Student student : students) {
                studentCombo.addItem(student.getRegistrationNumber() + " - " + student.getName());
            }
            studentCombo.setEditable(true);

            int result = JOptionPane.showConfirmDialog(this, studentCombo,
                    "Enter Student Registration Number:", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String selected = (String) studentCombo.getSelectedItem();
                String registrationNumber;

                if (selected.contains(" - ")) {
                    registrationNumber = selected.split(" - ")[0];
                } else {
                    registrationNumber = selected;
                }

                if (registrationNumber != null && !registrationNumber.trim().isEmpty()) {
                    // Check if book is available
                    BookController.BookAvailability availability = bookController.getBookAvailability(isbn);
                    if (availability != null && availability.getAvailableCopies() > 0) {
                        int borrow = JOptionPane.showConfirmDialog(this,
                                "Book is currently available!\n" +
                                        "Do you want to borrow it instead of reserving?",
                                "Book Available",
                                JOptionPane.YES_NO_OPTION);
                        if (borrow == JOptionPane.YES_OPTION) {
                            // Borrow instead of reserve
                            String borrowResult = bookController.borrowBook(isbn, registrationNumber);
                            if (borrowResult.equals("SUCCESS")) {
                                JOptionPane.showMessageDialog(this,
                                        "Book borrowed successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadAllBooks();
                                showBookAvailability();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        borrowResult,
                                        "Cannot Borrow",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            return;
                        }
                    }

                    // Proceed with reservation
                    String reserveResult = bookController.reserveBook(isbn, registrationNumber);
                    if (reserveResult.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(this,
                                "Book reserved successfully!\n" +
                                        "You will be notified when it becomes available.",
                                "Reservation Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                        showBookAvailability();
                        loadStudentReservations();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                reserveResult,
                                "Reservation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a book first!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadStudentReservations() {
        String selected = (String) reserveStudentCombo.getSelectedItem();
        if (selected != null && !selected.trim().isEmpty()) {
            String registrationNumber = selected.split(" - ")[0];
            List<String> reservations = bookController.getStudentReservations(registrationNumber);
            reservedBooksModel.clear();
            if (reservations.isEmpty()) {
                reservedBooksModel.addElement("No reservations found");
            } else {
                for (String reservation : reservations) {
                    reservedBooksModel.addElement(reservation);
                }
            }
        }
    }

    private void cancelReservation() {
        String selectedReservation = reservedBooksList.getSelectedValue();
        if (selectedReservation == null || selectedReservation.equals("No reservations found")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation to cancel!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedStudent = (String) reserveStudentCombo.getSelectedItem();
        if (selectedStudent == null) {
            return;
        }

        String registrationNumber = selectedStudent.split(" - ")[0];

        // Extract ISBN from reservation string (format: "ISBN - Title (Reserved on date)")
        String isbn = selectedReservation.split(" - ")[0];

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel reservation for:\n" + selectedReservation + "\n\nAre you sure?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String result = bookController.cancelReservation(isbn, registrationNumber);
            if (result.equals("SUCCESS")) {
                JOptionPane.showMessageDialog(this,
                        "Reservation cancelled successfully!",
                        "Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                loadStudentReservations();
                showBookAvailability();
            } else {
                JOptionPane.showMessageDialog(this,
                        result,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadStudentsForReservation() {
        reserveStudentCombo.removeAllItems();
        List<Student> students = bookController.getAllStudents();
        for (Student student : students) {
            reserveStudentCombo.addItem(student.getRegistrationNumber() + " - " + student.getName());
        }
    }
}