package uk.nhs.digital.nhsconnect.lab.results.mesh.exception;

import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

public class MeshRecipientUnknownException extends LabResultsBaseException {
    public MeshRecipientUnknownException(String message) {
        super(message);
    }
}
