package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticReportMapperTest {

    @Test
    void testMapMessageToDiagnosticReport() {
        final Message message = new Message(new ArrayList<>());

        assertThat(new DiagnosticReportMapper().map(message)).isExactlyInstanceOf(DiagnosticReport.class);
    }
}
