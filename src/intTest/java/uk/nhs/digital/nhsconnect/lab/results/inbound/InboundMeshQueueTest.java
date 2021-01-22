package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tests the processing of a REGISTRATION interchange by publishing it onto the inbound MESH message queue. This bypasses the
 * MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueTest extends IntegrationBaseTest {

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled(SoftAssertions softly) throws IOException, JMSException {
        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.REGISTRATION)
                .setContent(new String(Files.readAllBytes(edifactResource.getFile().toPath())))
                .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);
        final String expectedContent = new String(Files.readAllBytes(fhirResource.getFile().toPath()));

        softly.assertThat(message.getStringProperty("TransactionType")).isEqualTo(TransactionType.APPROVAL.name().toLowerCase());
        softly.assertThat(content).isEqualTo(expectedContent);
    }
}
