package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

public class FhirValidationException extends LabResultsBaseException {
    public FhirValidationException(String message) {
        super(message);
    }
}