package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ObservationMapperTest {

    @Test
    void testMapTestGroups() {
        final Message message = new Message(new ArrayList<>());

        assertFalse(new ObservationMapper().mapToTestGroups(message).isEmpty());
    }

    @Test
    void testMapTestResults() {
        final Message message = new Message(new ArrayList<>());

        assertFalse(new ObservationMapper().mapToTestResults(message).isEmpty());
    }
}
