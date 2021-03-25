package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the processing of a PATHOLOGY and SCREENING interchange by publishing it onto the inbound MESH message queue.
 * This bypasses the MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueTest extends IntegrationBaseTest {

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueuePathology2MessageIsReceivedThenMessageIsHandled()
            throws IOException, JMSException, JSONException {
        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_2)
            .setContent(new String(Files.readAllBytes(getPathology2EdifactResource().getFile().toPath())))
            .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);

        final String expectedContent = new String(Files.readAllBytes(getPathology2FhirResource().getFile().toPath()));

        assertThat(message.getStringProperty("Checksum")).isEqualTo("BAE9833404E34D8F67B3815FC4C51091");

        assertFhirEquals(expectedContent, content);
    }

    @Test
    void whenMeshInboundQueuePathology3MessageIsReceivedThenMessageIsHandled()
            throws IOException, JMSException, JSONException {
        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_3)
            .setContent(new String(Files.readAllBytes(getPathology3EdifactResource().getFile().toPath())))
            .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);

        final String expectedContent = new String(Files.readAllBytes(getPathology3FhirResource().getFile().toPath()));

        assertThat(message.getStringProperty("Checksum")).isEqualTo("BAE9833404E34D8F67B3815FC4C51091");

        assertFhirEquals(expectedContent, content);
    }

    @Test
    void whenMeshInboundQueueScreeningMessageIsReceivedThenMessageIsHandled()
            throws IOException, JMSException, JSONException {
        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.SCREENING)
            .setContent(new String(Files.readAllBytes(getScreeningEdifactResource().getFile().toPath())))
            .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);

        final String expectedContent = new String(Files.readAllBytes(getScreeningFhirResource().getFile().toPath()));

        assertThat(message.getStringProperty("Checksum")).isEqualTo("440A799A79EEDA64373DD4171FD5429D");

        assertFhirEquals(expectedContent, content);
    }
}
