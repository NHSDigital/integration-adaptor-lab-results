package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.PerformingOrgMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformingOrgMapperTest {

    @Test
    void testMapMessageToOrganization() {
        final Message message = new Message(new ArrayList<>());
        assertTrue(new PerformingOrgMapper().map(message).isEmpty());
    }
}
