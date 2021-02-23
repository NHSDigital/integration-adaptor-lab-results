package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

public class ToEdifactParsingException extends LabResultsBaseException {

    public ToEdifactParsingException(String message) {
        super(message);
    }

    public ToEdifactParsingException(String message, Exception exception) {
        super(message, exception);
    }
}
