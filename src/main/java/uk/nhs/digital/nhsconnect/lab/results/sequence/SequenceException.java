package uk.nhs.digital.nhsconnect.lab.results.sequence;

import uk.nhs.digital.nhsconnect.lab.results.rest.exception.BadRequestException;

public class SequenceException extends BadRequestException {
    public SequenceException(final String message) {
        super(message);
    }
}
