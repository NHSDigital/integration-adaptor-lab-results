package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowIdTest {

    @Test
    void testFromStringReturnsWorkflowIdForValidWorkflowIdString() {
        assertEquals(WorkflowId.REGISTRATION, WorkflowId.fromString(WorkflowId.REGISTRATION.getWorkflowId()));
    }

    @Test
    void testFromStringReturnsWorkflowIdForValidLowercaseWorkflowIdString() {
        final String lowercaseWorkflowId = WorkflowId.REGISTRATION.getWorkflowId().toLowerCase();
        assertEquals(WorkflowId.REGISTRATION, WorkflowId.fromString(lowercaseWorkflowId));
    }

    @Test
    void testFromStringThrowsExceptionForInvalidWorkflowIdString() {
        final MeshWorkflowUnknownException exception = assertThrows(MeshWorkflowUnknownException.class,
                () -> WorkflowId.fromString("INVALID"));
        assertEquals("Unsupported workflow id INVALID", exception.getMessage());
    }
}
