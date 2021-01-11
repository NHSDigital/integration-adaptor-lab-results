package uk.nhs.digital.nhsconnect.lab.results.rest.exception;

public class NhaisBaseException extends RuntimeException {

    public NhaisBaseException(String message) {
        super(message);
    }

    public NhaisBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NhaisBaseException(Throwable cause) {
        super(cause);
    }
}
