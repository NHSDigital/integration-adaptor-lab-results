package uk.nhs.digital.nhsconnect.lab.results.sequence;

import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

public class SequenceException extends LabResultsBaseException {
    public SequenceException(final String message) {
        super(message);
    }
}
