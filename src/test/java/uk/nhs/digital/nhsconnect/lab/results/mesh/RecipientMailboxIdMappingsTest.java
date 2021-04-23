package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshRecipientUnknownException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RecipientMailboxIdMappingsTest {

    private RecipientMailboxIdMappings recipientMailboxIdMappings;

    @BeforeEach
    void setUp() {
        recipientMailboxIdMappings = new RecipientMailboxIdMappings(
            "REC1=test_mailbox REC2=test_mailbox REC3=test_mailbox");
    }

    @Test
    void testGetRecipientMailboxIdForMessage() {
        assertEquals("test_mailbox", recipientMailboxIdMappings.getRecipientMailboxId("REC1"));
    }

    @Test
    void testGetRecipientMailboxIdForMessageRecipientNotFoundThrowsException() {
        final MeshRecipientUnknownException exception = assertThrows(MeshRecipientUnknownException.class,
            () -> recipientMailboxIdMappings.getRecipientMailboxId("INVALID"));

        assertEquals("Couldn't decode recipient: INVALID", exception.getMessage());
    }
}
