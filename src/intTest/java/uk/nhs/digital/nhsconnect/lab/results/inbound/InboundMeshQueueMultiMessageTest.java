package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.assertj.core.api.SoftAssertions;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
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

/**
 * Tests the processing of a PATHOLOGY interchange containing multiple messages by publishing it
 * onto the inbound MESH message queue. This bypasses the MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueMultiMessageTest extends IntegrationBaseTest {

    @Value("classpath:edifact/multi_pathology.edifact.dat")
    private Resource multiEdifactResource;

    @Value("classpath:edifact/multi_pathology_msg1.fhir.json")
    private Resource fhirMessage1;
    @Value("classpath:edifact/multi_pathology_msg2.fhir.json")
    private Resource fhirMessage2;
    @Value("classpath:edifact/multi_pathology_msg3.fhir.json")
    private Resource fhirMessage3;

    private String previousCorrelationId;

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled(SoftAssertions softly)
            throws IOException, JMSException, JSONException {

        final String content = new String(Files.readAllBytes(multiEdifactResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY)
            .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        assertGpOutboundQueueMessages(softly);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void assertGpOutboundQueueMessages(SoftAssertions softly) throws IOException, JMSException, JSONException {
        final List<Message> gpOutboundQueueMessages = List.of(getGpOutboundQueueMessage(), getGpOutboundQueueMessage(),
            getGpOutboundQueueMessage());

        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(0), fhirMessage1);
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(1), fhirMessage2);
        assertGpOutboundQueueMessages(softly, gpOutboundQueueMessages.get(2), fhirMessage3);
    }

    private void assertGpOutboundQueueMessages(SoftAssertions softly, Message message, Resource fhirMessage)
            throws IOException, JMSException, JSONException {

        // all messages come from the same interchange and use the same correlation id
        final String correlationId = message.getStringProperty("CorrelationId");
        if (previousCorrelationId == null) {
            previousCorrelationId = correlationId;
        }
        softly.assertThat(correlationId).isEqualTo(previousCorrelationId);

        final String messageBody = parseTextMessage(message);
        final String expectedMessageBody = new String(Files.readAllBytes(fhirMessage.getFile().toPath()));

        JSONAssert.assertEquals(
            expectedMessageBody,
            messageBody,
            new CustomComparator(
                JSONCompareMode.STRICT,
                new Customization("meta.lastUpdated", IGNORE),
                new Customization("identifier.value", IGNORE),
                new Customization("entry[*].fullUrl", IGNORE),
                new Customization("entry[*].resource.id", IGNORE)
            )
        );
    }

}
