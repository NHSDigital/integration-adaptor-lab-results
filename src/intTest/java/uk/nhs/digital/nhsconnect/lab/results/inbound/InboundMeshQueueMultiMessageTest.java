package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Tests the processing of a PATHOLOGY interchange containing multiple messages by publishing it
 * onto the inbound MESH message queue. This bypasses the MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueMultiMessageTest extends IntegrationBaseTest {

    @Value("classpath:edifact/multi_pathology.edifact.dat")
    private Resource multiEdifactResource;

    @Value("classpath:fhir/multi_pathology_msg1.fhir.json")
    private Resource fhirMessage1;
    @Value("classpath:fhir/multi_pathology_msg2.fhir.json")
    private Resource fhirMessage2;
    @Value("classpath:fhir/multi_pathology_msg3.fhir.json")
    private Resource fhirMessage3;

    private String previousCorrelationId;

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled() throws IOException {

        final String content = new String(Files.readAllBytes(multiEdifactResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_3)
            .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        assertGpOutboundQueueMessages();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void assertGpOutboundQueueMessages() {
        final List<Message> gpOutboundQueueMessages = List.of(getGpOutboundQueueMessage(), getGpOutboundQueueMessage(),
            getGpOutboundQueueMessage());

        var expectedFhirMessages = List.of(fhirMessage1, fhirMessage2, fhirMessage3);

        var assertions = expectedFhirMessages.stream()
            .map(expectedFhir -> {
                var index = expectedFhirMessages.indexOf(expectedFhir);
                return (Executable) () ->
                    assertGpOutboundQueueMessages(gpOutboundQueueMessages.get(index), expectedFhir);
            })
            .toArray(Executable[]::new);

        assertAll(assertions);
    }

    private void assertGpOutboundQueueMessages(Message message, Resource fhirMessage)
        throws IOException, JMSException, JSONException {

        // all messages come from the same interchange and use the same correlation id
        final String correlationId = message.getStringProperty("CorrelationId");
        if (previousCorrelationId == null) {
            previousCorrelationId = correlationId;
        }

        assertThat(correlationId).isEqualTo(previousCorrelationId);

        final String messageBody = parseTextMessage(message);
        final String expectedMessageBody = new String(Files.readAllBytes(fhirMessage.getFile().toPath()));
        assertFhirEquals(expectedMessageBody, messageBody);
    }

}
