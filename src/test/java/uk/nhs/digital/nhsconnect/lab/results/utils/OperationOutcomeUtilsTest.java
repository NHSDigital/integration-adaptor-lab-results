package uk.nhs.digital.nhsconnect.lab.results.utils;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationOutcomeUtilsTest {

    @Test
    void testCreateUnknownError() {
        final OperationOutcome operationOutcome = OperationOutcomeUtils.createFromMessage("unknown error");
        final OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.getIssueFirstRep();
        assertEquals(OperationOutcome.IssueSeverity.ERROR, issue.getSeverity());
        assertEquals(OperationOutcome.IssueType.UNKNOWN, issue.getCode());
        assertEquals("unknown error", issue.getDetails().getText());
    }

    @Test
    void testCreateErrorWithSpecificType() {
        final OperationOutcome operationOutcome =
                OperationOutcomeUtils.createFromMessage("structure error", OperationOutcome.IssueType.STRUCTURE);
        final OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.getIssueFirstRep();
        assertEquals(OperationOutcome.IssueSeverity.ERROR, issue.getSeverity());
        assertEquals(OperationOutcome.IssueType.STRUCTURE, issue.getCode());
        assertEquals("structure error", issue.getDetails().getText());
    }
}
