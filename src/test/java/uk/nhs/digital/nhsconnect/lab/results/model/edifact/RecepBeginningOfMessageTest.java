package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecepBeginningOfMessageTest {

    private RecepBeginningOfMessage recepBeginningOfMessage;

    @BeforeEach
    void setUp() {
        recepBeginningOfMessage = new RecepBeginningOfMessage();
    }

    @Test
    void testGetKey() {
        assertEquals("BGM", recepBeginningOfMessage.getKey());
    }

    @Test
    void testGetValue() {
        recepBeginningOfMessage.setTimestamp(Instant.parse("2020-01-26T14:32:49Z"));
        assertEquals("+600+243:202001261432:306+64", recepBeginningOfMessage.getValue());
    }
}
