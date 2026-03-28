package utilis;

public class DuplicateEntryException extends Exception {
    private String field;
    private String value;

    public DuplicateEntryException(String message, String field, String value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}