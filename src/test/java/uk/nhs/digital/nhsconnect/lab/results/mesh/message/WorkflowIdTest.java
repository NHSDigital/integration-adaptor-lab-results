package uk.nhs.digital.nhsconnect.lab.results.mesh.message;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowIdTest {

    @Test
    void testFromStringReturnsWorkflowIdForValidWorkflowIdString() {
        assertAll(
            () -> assertEquals(WorkflowId.PATHOLOGY_2,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_2.getWorkflowId())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3_ACK,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3_ACK.getWorkflowId())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3.getWorkflowId())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3_ACK,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3_ACK.getWorkflowId())),
            () -> assertEquals(WorkflowId.SCREENING,
                WorkflowId.fromString(WorkflowId.SCREENING.getWorkflowId())),
            () -> assertEquals(WorkflowId.SCREENING_ACK,
                WorkflowId.fromString(WorkflowId.SCREENING_ACK.getWorkflowId()))
        );
    }

    @Test
    void testFromStringReturnsWorkflowIdForValidLowercaseWorkflowIdString() {
        assertAll(
            () -> assertEquals(WorkflowId.PATHOLOGY_2,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_2.getWorkflowId().toLowerCase())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3_ACK,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3_ACK.getWorkflowId().toLowerCase())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3.getWorkflowId().toLowerCase())),
            () -> assertEquals(WorkflowId.PATHOLOGY_3_ACK,
                WorkflowId.fromString(WorkflowId.PATHOLOGY_3_ACK.getWorkflowId().toLowerCase())),
            () -> assertEquals(WorkflowId.SCREENING,
                WorkflowId.fromString(WorkflowId.SCREENING.getWorkflowId().toLowerCase())),
            () -> assertEquals(WorkflowId.SCREENING_ACK,
                WorkflowId.fromString(WorkflowId.SCREENING_ACK.getWorkflowId().toLowerCase()))
        );
    }

    @Test
    void testFromStringThrowsExceptionForInvalidWorkflowIdString() {
        final MeshWorkflowUnknownException exception = assertThrows(MeshWorkflowUnknownException.class,
            () -> WorkflowId.fromString("INVALID"));
        assertEquals("Unknown workflow id INVALID", exception.getMessage());
    }
}
