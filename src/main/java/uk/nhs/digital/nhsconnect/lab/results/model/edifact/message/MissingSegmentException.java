package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

public class MissingSegmentException extends ToEdifactParsingException {

    public MissingSegmentException(String message) {
        super(message);
    }
}
