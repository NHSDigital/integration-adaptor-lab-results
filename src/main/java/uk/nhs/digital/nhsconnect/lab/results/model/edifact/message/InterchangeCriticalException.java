package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

public class InterchangeCriticalException extends RuntimeException {
    public InterchangeCriticalException(Exception ex) {
        super(ex);
    }

    public InterchangeCriticalException(String message, Exception exception) {
        super(message, exception);
    }
}
