package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.RequesterMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequesterMapperTest {

    @Test
    void testMapMessageToPractitioner() {
        final Message message = new Message(new ArrayList<>());
        assertTrue(new RequesterMapper().map(message).isEmpty());
    }
}
