package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests the processing of a PATHOLOGY interchange containing multiple messages by publishing it
 * onto the inbound MESH message queue. This bypasses the MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueMultiMessageTest extends IntegrationBaseTest {

    @Value("classpath:edifact/multi_pathology.dat")
    private Resource multiEdifactResource;

    private String previousCorrelationId;

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled(SoftAssertions softly)
        throws IOException, JMSException {

        final String content = new String(Files.readAllBytes(multiEdifactResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY)
            .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        assertGpOutboundQueueMessages(softly);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void assertGpOutboundQueueMessages(SoftAssertions softly) throws JMSException, IOException {
        final List<Message> gpOutboundQueueMessages = IntStream.range(0, 6)
            .mapToObj(x -> getGpOutboundQueueMessage())
            .collect(Collectors.toList());

        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(0));
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(1));
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(2));
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(3));
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(4));
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(5));
    }

    private void assertGpOutboundQueueMessages(SoftAssertions softly, Message message)
        throws JMSException, IOException {

        // all messages come from the same interchange and use the same correlation id
        final String correlationId = message.getStringProperty("CorrelationId");
        if (previousCorrelationId == null) {
            previousCorrelationId = correlationId;
        }
        softly.assertThat(correlationId).isEqualTo(previousCorrelationId);

        final String messageBody = parseTextMessage(message);
        final String expectedMessageBody = new String(Files.readAllBytes(getFhirResource().getFile().toPath()));
        softly.assertThat(messageBody).isEqualTo(expectedMessageBody);
    }

}
