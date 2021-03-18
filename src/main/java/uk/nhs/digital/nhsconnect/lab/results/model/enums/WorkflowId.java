package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum WorkflowId {
    PATHOLOGY_2("PATH_MEDRPT_V2"),
    PATHOLOGY_2_ACK("PATH_MEDRPT_V2_ACK"),
    PATHOLOGY_3("PATH_MEDRPT_V3"),
    PATHOLOGY_3_ACK("PATH_MEDRPT_V3_ACK"),
    SCREENING("SCRN_BCS_MEDRPT_V4"),
    SCREENING_ACK("SCRN_BCS_MEDRPT_V4_ACK");

    @JsonValue
    private final String workflowId;

    @Override
    public String toString() {
        return workflowId;
    }

    public static WorkflowId fromString(String workflowId) {
        return Arrays.stream(WorkflowId.values())
            .filter(workflow -> workflow.workflowId.equalsIgnoreCase(workflowId))
            .findFirst()
            .orElseThrow(() -> new MeshWorkflowUnknownException("Unknown workflow id " + workflowId, workflowId));
    }
}
