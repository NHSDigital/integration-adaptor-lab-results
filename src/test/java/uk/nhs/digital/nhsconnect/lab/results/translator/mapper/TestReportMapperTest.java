package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.TestReportMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestReportMapperTest {

    @Test
    void testMapMessageToDiagnosticReport() {
        final Message message = new Message(new ArrayList<>());
        assertTrue(new TestReportMapper().map(message).isEmpty());
    }
}
