package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshRecipientUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RecipientMailboxIdMappingsTest {

    private RecipientMailboxIdMappings recipientMailboxIdMappings;

    @BeforeEach
    void setUp() {
        recipientMailboxIdMappings = new RecipientMailboxIdMappings("REC1=test_mailbox REC2=test_mailbox REC3=test_mailbox");
    }

    @Test
    void testGetRecipientMailboxIdForMessage() {

        final OutboundMeshMessage message = new MeshMessage().setHaTradingPartnerCode("REC1");

        assertEquals("test_mailbox", recipientMailboxIdMappings.getRecipientMailboxId(message));
    }

    @Test
    void testGetRecipientMailboxIdForMessageRecipientNotFoundThrowsException() {

        final OutboundMeshMessage message = new MeshMessage().setHaTradingPartnerCode("INVALID");

        final MeshRecipientUnknownException exception = assertThrows(MeshRecipientUnknownException.class,
                () -> recipientMailboxIdMappings.getRecipientMailboxId(message));

        assertEquals("Couldn't decode recipient: INVALID", exception.getMessage());
    }

    @Test
    void testGetRecipientMailboxIdForMessageNoRecipientToMailboxMappingsThrowsException() {

        recipientMailboxIdMappings = new RecipientMailboxIdMappings("");

        final OutboundMeshMessage message = new MeshMessage();

        final MeshRecipientUnknownException exception = assertThrows(MeshRecipientUnknownException.class,
                () -> recipientMailboxIdMappings.getRecipientMailboxId(message));

        assertEquals("LAB_RESULTS_MESH_RECIPIENT_MAILBOX_ID_MAPPINGS env var doesn't contain valid "
            +
                "recipient to mailbox mapping", exception.getMessage());
    }

}
