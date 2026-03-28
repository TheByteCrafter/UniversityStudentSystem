package controller;

import model.*;
import utilis.DatabaseConnection;
import utilis.FileStorage;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class BookController {
    private DatabaseConnection dbConnection;
    private FileStorage fileStorage;
    private Map<String, Book> books;
    private Map<String, List<Reservation>> reservations;
    private Map<String, List<BorrowRecord>> borrowRecords;
    private StudentController studentController;

    public BookController() {
        dbConnection = DatabaseConnection.getInstance();
        fileStorage = new FileStorage("books.dat");
        books = new HashMap<>();
        reservations = new HashMap<>();
        borrowRecords = new HashMap<>();
        studentController = new StudentController();
        loadFromFile();
        loadFromDatabase();
    }

    // Inner class for Book Availability
    public static class BookAvailability {
        private int totalCopies;
        private int availableCopies;
        private int borrowedCopies;
        private int reservedCopies;
        private int overdueCopies;

        public BookAvailability(int total, int available, int borrowed, int reserved, int overdue) {
            this.totalCopies = total;
            this.availableCopies = available;
            this.borrowedCopies = borrowed;
            this.reservedCopies = reserved;
            this.overdueCopies = overdue;
        }

        // Getters
        public int getTotalCopies() { return totalCopies; }
        public int getAvailableCopies() { return availableCopies; }
        public int getBorrowedCopies() { return borrowedCopies; }
        public int getReservedCopies() { return reservedCopies; }
        public int getOverdueCopies() { return overdueCopies; }
    }

    public void addBook(Book book) {
        books.put(book.getIsbn(), book);
        saveToFile();
        saveToDatabase(book);
    }

    public List<Book> searchBooksByTitle(String title) {
        String searchTerm = title.toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(searchTerm)) {
                results.add(book);
            }
        }
        return results;
    }

    public Book getBookByIsbn(String isbn) {
        return books.get(isbn);
    }

    public BookAvailability getBookAvailability(String isbn) {
        Book book = books.get(isbn);
        if (book != null) {
            int total = book.getTotalCopies();
            int borrowed = book.getBorrowedCopies();
            int reserved = getReservationCount(isbn);
            int overdue = getOverdueCount(isbn);
            int available = total - borrowed;

            return new BookAvailability(total, available, borrowed, reserved, overdue);
        }
        return null;
    }



    public String borrowBook(String isbn, String registrationNumber) {
        Book book = books.get(isbn);
        if (book == null) {
            return "Book not found!";
        }

        // Validate student by registration number
        Student student = studentController.getStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            return "Student not found! Please enter a valid Registration Number.";
        }

        String studentId = student.getId(); // Get internal ID for database operations

        // Check if student has overdue books
        if (hasOverdueBooks(studentId)) {
            return "Cannot borrow: You have overdue books! Please return them first.";
        }

        // Check if student already borrowed this book
        if (hasBorrowedBook(isbn, studentId)) {
            return "You have already borrowed this book!";
        }

        // Check if student has reached maximum borrow limit (e.g., 5 books)
        if (getStudentBorrowCount(studentId) >= 5) {
            return "Cannot borrow: You have already borrowed the maximum number of books (5)!";
        }

        int available = book.getTotalCopies() - book.getBorrowedCopies();
        if (available > 0) {
            // Borrow the book
            book.setBorrowedCopies(book.getBorrowedCopies() + 1);

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 14); // 14 days borrowing period

            BorrowRecord record = new BorrowRecord(isbn, studentId, new Date(), cal.getTime());
            borrowRecords.computeIfAbsent(studentId, k -> new ArrayList<>()).add(record);

            saveToFile();
            saveBorrowToDatabase(isbn, studentId, record);

            return "SUCCESS";
        } else {
            return "Book is not available for borrowing! Consider reserving it.";
        }
    }

    public String returnBook(String isbn, String registrationNumber) {
        Book book = books.get(isbn);
        if (book == null) {
            return "Book not found!";
        }

        // Validate student by registration number
        Student student = studentController.getStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            return "Student not found! Please enter a valid Registration Number.";
        }

        String studentId = student.getId();

        if (book.getBorrowedCopies() > 0) {
            // Find the borrow record
            List<BorrowRecord> records = borrowRecords.get(studentId);
            if (records != null) {
                BorrowRecord foundRecord = null;
                for (BorrowRecord record : records) {
                    if (record.getBookIsbn().equals(isbn) && record.getReturnDate() == null) {
                        foundRecord = record;
                        break;
                    }
                }

                if (foundRecord != null) {
                    foundRecord.setReturnDate(new Date());
                    book.setBorrowedCopies(book.getBorrowedCopies() - 1);

                    // Check if overdue
                    if (foundRecord.isOverdue()) {
                        calculateFine(foundRecord);
                    }

                    saveToFile();
                    updateReturnInDatabase(isbn, studentId);

                    return "SUCCESS";
                }
            }
            return "Borrow record not found! This student may not have borrowed this book.";
        }
        return "No copies of this book are currently borrowed!";
    }

    public String reserveBook(String isbn, String registrationNumber) {
        Book book = books.get(isbn);
        if (book == null) {
            return "Book not found!";
        }

        // Validate student by registration number
        Student student = studentController.getStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            return "Student not found! Please enter a valid Registration Number.";
        }

        String studentId = student.getId();

        // Check if student already has a reservation for this book
        if (hasReservation(isbn, studentId)) {
            return "You already have a reservation for this book!";
        }

        // Check if student already has too many reservations (max 3)
        if (getStudentReservationCount(studentId) >= 3) {
            return "You already have 3 reserved books! Cannot reserve more.";
        }

        // Create reservation
        Reservation reservation = new Reservation(isbn, studentId, new Date());
        reservations.computeIfAbsent(isbn, k -> new ArrayList<>()).add(reservation);

        saveToFile();
        saveReservationToDatabase(reservation);

        return "SUCCESS";
    }

    public String cancelReservation(String isbn, String registrationNumber) {
        // Validate student by registration number
        Student student = studentController.getStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            return "Student not found!";
        }

        String studentId = student.getId();

        List<Reservation> bookReservations = reservations.get(isbn);
        if (bookReservations != null) {
            boolean removed = bookReservations.removeIf(r -> r.getStudentId().equals(studentId));
            if (removed) {
                saveToFile();
                removeReservationFromDatabase(isbn, studentId);
                return "SUCCESS";
            }
        }
        return "Reservation not found!";
    }

    public List<String> getStudentReservations(String registrationNumber) {
        // Validate student by registration number
        Student student = studentController.getStudentByRegistrationNumber(registrationNumber);
        if (student == null) {
            return new ArrayList<>();
        }

        String studentId = student.getId();
        List<String> studentReservations = new ArrayList<>();
        for (Map.Entry<String, List<Reservation>> entry : reservations.entrySet()) {
            for (Reservation r : entry.getValue()) {
                if (r.getStudentId().equals(studentId)) {
                    Book book = books.get(entry.getKey());
                    if (book != null) {
                        studentReservations.add(book.getIsbn() + " - " + book.getTitle() +
                                " (Reserved on: " + r.getReservationDate() + ")");
                    }
                }
            }
        }
        return studentReservations;
    }



    public boolean hasReservationsForBook(String isbn) {
        List<Reservation> bookReservations = reservations.get(isbn);
        return bookReservations != null && !bookReservations.isEmpty();
    }

    public void notifyNextReservation(String isbn) {
        List<Reservation> bookReservations = reservations.get(isbn);
        if (bookReservations != null && !bookReservations.isEmpty()) {
            Reservation next = bookReservations.get(0);
            // In a real system, you would send email/SMS notification
            System.out.println("Notifying student " + next.getStudentId() +
                    " that book " + isbn + " is now available!");

            // Remove from reservations since they've been notified
            bookReservations.remove(0);
            saveToFile();
            removeReservationFromDatabase(isbn, next.getStudentId());
        }
    }

    public List<Student> getAllStudents() {
        return studentController.getAllStudents();
    }

    private boolean hasReservation(String isbn, String studentId) {
        List<Reservation> bookReservations = reservations.get(isbn);
        if (bookReservations != null) {
            return bookReservations.stream().anyMatch(r -> r.getStudentId().equals(studentId));
        }
        return false;
    }

    private int getStudentReservationCount(String studentId) {
        int count = 0;
        for (List<Reservation> bookReservations : reservations.values()) {
            count += bookReservations.stream().filter(r -> r.getStudentId().equals(studentId)).count();
        }
        return count;
    }

    private int getReservationCount(String isbn) {
        List<Reservation> bookReservations = reservations.get(isbn);
        return bookReservations != null ? bookReservations.size() : 0;
    }

    private int getOverdueCount(String isbn) {
        int count = 0;
        for (List<BorrowRecord> records : borrowRecords.values()) {
            for (BorrowRecord record : records) {
                if (record.getBookIsbn().equals(isbn) && record.isOverdue()) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean hasOverdueBooks(String studentId) {
        List<BorrowRecord> records = borrowRecords.get(studentId);
        if (records != null) {
            for (BorrowRecord record : records) {
                if (record.isOverdue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasBorrowedBook(String isbn, String studentId) {
        List<BorrowRecord> records = borrowRecords.get(studentId);
        if (records != null) {
            for (BorrowRecord record : records) {
                if (record.getBookIsbn().equals(isbn) && record.getReturnDate() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getStudentBorrowCount(String studentId) {
        List<BorrowRecord> records = borrowRecords.get(studentId);
        if (records != null) {
            return (int) records.stream().filter(r -> r.getReturnDate() == null).count();
        }
        return 0;
    }

    private void calculateFine(BorrowRecord record) {
        // Calculate fine (e.g., 50 shillings per day overdue)
        long daysOverdue = (new Date().getTime() - record.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
        double fine = daysOverdue * 50;
        System.out.println("Fine for student " + record.getStudentId() + ": KSh " + fine);
    }

    private void saveToDatabase(Book book) {
        String query = "INSERT INTO books (isbn, title, edition, version, year_published, " +
                "total_copies, borrowed_copies) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setInt(3, book.getEdition());
            pstmt.setString(4, book.getVersion());
            pstmt.setInt(5, book.getYearPublished());
            pstmt.setInt(6, book.getTotalCopies());
            pstmt.setInt(7, book.getBorrowedCopies());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveBorrowToDatabase(String isbn, String studentId, BorrowRecord record) {
        String query = "INSERT INTO borrow_records (book_isbn, student_id, borrow_date, due_date) " +
                "VALUES (?, ?, ?, ?)";
        String updateBook = "UPDATE books SET borrowed_copies = borrowed_copies + 1 WHERE isbn = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, studentId);
            pstmt.setDate(3, new java.sql.Date(record.getBorrowDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(record.getDueDate().getTime()));
            pstmt.executeUpdate();

            try (PreparedStatement pstmt2 = dbConnection.getConnection().prepareStatement(updateBook)) {
                pstmt2.setString(1, isbn);
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateReturnInDatabase(String isbn, String studentId) {
        String query = "UPDATE borrow_records SET return_date = NOW() " +
                "WHERE book_isbn = ? AND student_id = ? AND return_date IS NULL";
        String updateBook = "UPDATE books SET borrowed_copies = borrowed_copies - 1 WHERE isbn = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, studentId);
            pstmt.executeUpdate();

            try (PreparedStatement pstmt2 = dbConnection.getConnection().prepareStatement(updateBook)) {
                pstmt2.setString(1, isbn);
                pstmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveReservationToDatabase(Reservation reservation) {
        String query = "INSERT INTO reservations (book_isbn, student_id, reservation_date) " +
                "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, reservation.getBookIsbn());
            pstmt.setString(2, reservation.getStudentId());
            pstmt.setDate(3, new java.sql.Date(reservation.getReservationDate().getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeReservationFromDatabase(String isbn, String studentId) {
        String query = "DELETE FROM reservations WHERE book_isbn = ? AND student_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDatabase() {
        // Load books
        String query = "SELECT * FROM books";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getInt("edition"),
                        rs.getString("version"),
                        rs.getInt("year_published"),
                        rs.getInt("total_copies")
                );
                book.setBorrowedCopies(rs.getInt("borrowed_copies"));
                books.put(book.getIsbn(), book);
            }

            // Load borrow records
            loadBorrowRecords();

            // Load reservations
            loadReservations();

            System.out.println("Loaded " + books.size() + " books from database");
        } catch (SQLException e) {
            System.err.println("Error loading books from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBorrowRecords() {
        String query = "SELECT * FROM borrow_records";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                BorrowRecord record = new BorrowRecord(
                        rs.getString("book_isbn"),
                        rs.getString("student_id"),
                        rs.getDate("borrow_date"),
                        rs.getDate("due_date")
                );
                Date returnDate = rs.getDate("return_date");
                if (returnDate != null) {
                    record.setReturnDate(returnDate);
                }
                borrowRecords.computeIfAbsent(record.getStudentId(), k -> new ArrayList<>()).add(record);
            }
            System.out.println("Loaded borrow records");
        } catch (SQLException e) {
            System.err.println("Error loading borrow records: " + e.getMessage());
        }
    }

    private void loadReservations() {
        String query = "SELECT * FROM reservations";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getString("book_isbn"),
                        rs.getString("student_id"),
                        rs.getDate("reservation_date")
                );
                reservations.computeIfAbsent(reservation.getBookIsbn(), k -> new ArrayList<>()).add(reservation);
            }
            System.out.println("Loaded reservations");
        } catch (SQLException e) {
            System.err.println("Error loading reservations: " + e.getMessage());
        }
    }

    private void saveToFile() {
        fileStorage.save(books);
    }

    private void loadFromFile() {
        Object obj = fileStorage.load();
        if (obj instanceof Map) {
            books = (Map<String, Book>) obj;
            System.out.println("Loaded " + books.size() + " books from file");
        }
    }
}