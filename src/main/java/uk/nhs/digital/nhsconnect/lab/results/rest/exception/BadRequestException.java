package uk.nhs.digital.nhsconnect.lab.results.rest.exception;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import uk.nhs.digital.nhsconnect.lab.results.utils.OperationOutcomeUtils;

public abstract class BadRequestException extends LabResultsBaseException {

    private final IBaseOperationOutcome operationOutcome;

    public BadRequestException(String message) {
        super(message);
        operationOutcome = OperationOutcomeUtils.createFromMessage(message, getIssueType());
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        operationOutcome = OperationOutcomeUtils.createFromMessage(message, getIssueType());
    }

    public BadRequestException(Throwable cause) {
        super(cause);
        operationOutcome = OperationOutcomeUtils.createFromMessage(cause.getMessage(), getIssueType());
    }

    public OperationOutcome.IssueType getIssueType() {
        return OperationOutcome.IssueType.INVALID;
    }
}
