package model;

import java.io.Serializable;
import java.util.Date;

public class Reservation implements Serializable {
    private String bookIsbn;
    private String studentId;
    private Date reservationDate;
    private boolean notified;

    public Reservation(String bookIsbn, String studentId, Date reservationDate) {
        this.bookIsbn = bookIsbn;
        this.studentId = studentId;
        this.reservationDate = reservationDate;
        this.notified = false;
    }

    // Getters and Setters
    public String getBookIsbn() { return bookIsbn; }
    public String getStudentId() { return studentId; }
    public Date getReservationDate() { return reservationDate; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}