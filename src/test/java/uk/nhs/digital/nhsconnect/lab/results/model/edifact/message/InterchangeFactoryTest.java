package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterchangeFactoryTest {
    private InterchangeFactory interchangeFactory;

    @BeforeEach
    void setUp() {
        this.interchangeFactory = new InterchangeFactory();
    }

    @Test
    void testCreate() {
        final List<String> strings = List.of("foo", "bar");
        Interchange result = interchangeFactory.createInterchange(strings);
        assertEquals(strings, result.getEdifactSegments());
    }
}
