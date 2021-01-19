package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;


import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test EDIFACT message is sent to the MESH mailbox where the adaptor receives
 * inbound transactions. The test waits for the transaction to be processed and compares
 * the message from the inbound queue to be the same to the message which has been sent.
 */

public class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "XX11";
    private static final String CONTENT = "test_message";
    private static final OutboundMeshMessage OUTBOUND_MESH_MESSAGE = OutboundMeshMessage.create(
            RECIPIENT, WorkflowId.REGISTRATION, CONTENT, null, null
    );

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
        clearInboundQueue();
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "true"); //enable scheduling
    }

    @AfterEach
    void tearDown() {
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "false");
    }

    @Test
    void testFetchMessageAndPushItToTheInboundQueue() throws JMSException {
        // Acting as an LAB_RESULTS system, send EDIFACT to adaptor's MESH mailbox
        meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        Message inboundQueueMessage = getInboundQueueMessage();

        assertThat(inboundQueueMessage).isNotNull();
        assertThat(parseTextMessage(inboundQueueMessage)).contains(CONTENT);
    }
}
