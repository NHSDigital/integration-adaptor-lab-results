package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTypeTest {

    @Test
    void testFromStringReturnsMessageTypeForValidMessageTypeString() {
        assertAll(
            () -> assertEquals(MessageType.NHSACK, MessageType.fromCode("NHS001")),
            () -> assertEquals(MessageType.PATHOLOGY, MessageType.fromCode("NHS003")),
            () -> assertEquals(MessageType.SCREENING, MessageType.fromCode("NHS004"))
        );
    }

    @Test
    void testFromStringThrowsExceptionForInvalidMessageTypeString() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> MessageType.fromCode("INVALID"));

        assertEquals("No message type for \"INVALID\"", exception.getMessage());
    }
}
