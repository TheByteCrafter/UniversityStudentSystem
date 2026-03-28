package model;

import java.io.Serializable;

public class Book implements Serializable {
    private String isbn;
    private String title;
    private int edition;
    private String version;
    private int yearPublished;
    private int totalCopies;
    private int borrowedCopies;

    public Book(String isbn, String title, int edition, String version,
                int yearPublished, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.edition = edition;
        this.version = version;
        this.yearPublished = yearPublished;
        this.totalCopies = totalCopies;
        this.borrowedCopies = 0;
    }

    public boolean isAvailable() {
        return borrowedCopies < totalCopies;
    }

    public int getAvailableCopies() {
        return totalCopies - borrowedCopies;
    }

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public int getEdition() { return edition; }
    public String getVersion() { return version; }
    public int getYearPublished() { return yearPublished; }
    public int getTotalCopies() { return totalCopies; }
    public int getBorrowedCopies() { return borrowedCopies; }
    public void setBorrowedCopies(int borrowedCopies) { this.borrowedCopies = borrowedCopies; }
}