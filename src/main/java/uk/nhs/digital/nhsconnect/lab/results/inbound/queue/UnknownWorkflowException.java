package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

class UnknownWorkflowException extends LabResultsBaseException {
    UnknownWorkflowException(Object workflowId) {
        super("Unknown workflow id: " + workflowId);
    }
}
