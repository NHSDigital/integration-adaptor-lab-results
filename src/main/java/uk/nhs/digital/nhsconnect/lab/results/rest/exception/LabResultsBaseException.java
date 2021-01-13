package uk.nhs.digital.nhsconnect.lab.results.rest.exception;

public class LabResultsBaseException extends RuntimeException {

    public LabResultsBaseException(String message) {
        super(message);
    }

    public LabResultsBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LabResultsBaseException(Throwable cause) {
        super(cause);
    }
}
