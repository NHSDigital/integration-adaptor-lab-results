package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Inbound;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test EDIFACT message is sent to the MESH mailbox where the adaptor receives
 * inbound transactions. The test waits for the transaction to be processed and compares
 * the message from the inbound queue to be the same to the message which has been sent.
 */

@Slf4j
public class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "XX11";

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
        clearGpOutboundQueue();
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "true"); //enable scheduling
    }

    @AfterEach
    void tearDown() {
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "false");
    }

    @Test
    void testSendEdifactIsProcessedAndPushedToGpOutboundQueue() throws JMSException, IOException {

        final String content = new String(Files.readAllBytes(edifactResource.getFile().toPath()));

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(RECIPIENT,
                WorkflowId.REGISTRATION, content, null, null);

        labResultsMeshClient.sendEdifactMessage(outboundMeshMessage);

        final Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
        assertThat(gpOutboundQueueMessage).isNotNull();

        final String transactionType = gpOutboundQueueMessage.getStringProperty(JmsHeaders.TRANSACTION_TYPE);
        assertThat(transactionType).isEqualTo(Inbound.APPROVAL.name().toLowerCase());

        final String conservationId = gpOutboundQueueMessage.getStringProperty(JmsHeaders.CONVERSATION_ID);
        assertThat(conservationId).isNotEmpty();

        final String expectedMessageBody = new String(Files.readAllBytes(fhirResource.getFile().toPath()));
        final String messageBody = parseTextMessage(gpOutboundQueueMessage);
        assertThat(messageBody).isEqualTo(expectedMessageBody);
    }
}
