package model;

import java.util.Date;

public class BorrowRecord {
    private String bookIsbn;
    private String studentId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private boolean isOverdue;

    public BorrowRecord(String bookIsbn, String studentId, Date borrowDate, Date dueDate) {
        this.bookIsbn = bookIsbn;
        this.studentId = studentId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.isOverdue = false;
    }

    public boolean isOverdue() {
        if (returnDate != null) return false;
        return new Date().after(dueDate);
    }

    // Getters and Setters

    public Date getBorrowDate() {
        return borrowDate;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
