package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import uk.nhs.digital.nhsconnect.lab.results.rest.exception.BadRequestException;

public class ToEdifactParsingException extends BadRequestException {

    public ToEdifactParsingException(String message) {
        super(message);
    }

}
