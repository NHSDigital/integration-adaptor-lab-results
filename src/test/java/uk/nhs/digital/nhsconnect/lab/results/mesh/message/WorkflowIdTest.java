package uk.nhs.digital.nhsconnect.lab.results.mesh.message;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowIdTest {

    @Test
    void testFromStringReturnsWorkflowIdForValidWorkflowIdString() {
        assertAll(
                () -> assertEquals(WorkflowId.PATHOLOGY,
                        WorkflowId.fromString(WorkflowId.PATHOLOGY.getWorkflowId())),
                () -> assertEquals(WorkflowId.PATHOLOGY_ACK,
                        WorkflowId.fromString(WorkflowId.PATHOLOGY_ACK.getWorkflowId())),
                () -> assertEquals(WorkflowId.SCREENING,
                        WorkflowId.fromString(WorkflowId.SCREENING.getWorkflowId())),
                () -> assertEquals(WorkflowId.SCREENING_ACK,
                        WorkflowId.fromString(WorkflowId.SCREENING_ACK.getWorkflowId()))
        );
    }

    @Test
    void testFromStringReturnsWorkflowIdForValidLowercaseWorkflowIdString() {
        assertAll(
                () -> assertEquals(WorkflowId.PATHOLOGY,
                        WorkflowId.fromString(WorkflowId.PATHOLOGY.getWorkflowId().toLowerCase())),
                () -> assertEquals(WorkflowId.PATHOLOGY_ACK,
                        WorkflowId.fromString(WorkflowId.PATHOLOGY_ACK.getWorkflowId().toLowerCase())),
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
        assertEquals("Unsupported workflow id INVALID", exception.getMessage());
    }
}
