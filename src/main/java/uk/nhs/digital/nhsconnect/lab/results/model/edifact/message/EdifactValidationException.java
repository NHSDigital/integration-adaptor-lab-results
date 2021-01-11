package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

public class EdifactValidationException extends ToEdifactParsingException {
    public EdifactValidationException(String message)
    {
        super(message);
    }
}
