package uk.nhs.digital.nhsconnect.lab.results.mesh.exception;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.exception.LabResultsBaseException;

@Getter
public class MeshWorkflowUnknownException extends LabResultsBaseException {

    private final String workflowId;

    public MeshWorkflowUnknownException(String message, String workflowId) {
        super(message);
        this.workflowId = workflowId;
    }
}
