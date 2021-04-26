package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshRecipientUnknownException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RecipientMailboxIdMappingsTest {

    private RecipientMailboxIdMappings recipientMailboxIdMappings;

    @BeforeEach
    void setUp() {
        recipientMailboxIdMappings = new RecipientMailboxIdMappings(
            "REC1=test_mailbox REC2=test_mailbox REC3=test_mailbox");
    }

    @Test
    void testGetRecipientMailboxIdForMessage() {
        assertThat(recipientMailboxIdMappings.getRecipientMailboxId("REC1")).isEqualTo("test_mailbox");
    }

    @Test
    void testGetRecipientMailboxIdForMessageRecipientNotFoundThrowsException() {
        assertThatThrownBy(() -> recipientMailboxIdMappings.getRecipientMailboxId("INVALID"))
            .isInstanceOf(MeshRecipientUnknownException.class)
            .hasMessage("Couldn't decode recipient: INVALID");
    }

    @Test
    void testGetRecipientMailboxIdForMessageNoRecipientToMailboxMappingsThrowsException() {
        recipientMailboxIdMappings = new RecipientMailboxIdMappings("");

        assertThatThrownBy(() -> recipientMailboxIdMappings.getRecipientMailboxId("REC1"))
            .isInstanceOf(MeshRecipientUnknownException.class)
            .hasMessage("LAB_RESULTS_MESH_RECIPIENT_MAILBOX_ID_MAPPINGS env var doesn't contain valid "
                + "recipient to mailbox mapping");
    }
}
