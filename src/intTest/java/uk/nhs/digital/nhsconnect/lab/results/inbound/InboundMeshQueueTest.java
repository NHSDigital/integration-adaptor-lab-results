package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tests the processing of a PATHOLOGY interchange by publishing it onto the inbound MESH message queue.
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
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled() throws IOException, JMSException, JSONException {
        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY)
            .setContent(new String(Files.readAllBytes(getEdifactResource().getFile().toPath())))
            .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);

        final String expectedContent = new String(Files.readAllBytes(getFhirResource().getFile().toPath()));

        JSONAssert.assertEquals(
            expectedContent,
            content,
            new CustomComparator(
                JSONCompareMode.STRICT,
                new Customization("id", IGNORE),
                new Customization("meta.lastUpdated", IGNORE),
                new Customization("identifier.value", IGNORE),
                new Customization("entry[*].fullUrl", IGNORE),
                new Customization("entry[*].resource.subject.reference", IGNORE),
                new Customization("entry[*].resource.related[*].target.reference", IGNORE),
                new Customization("entry[*].resource.id", IGNORE)
            )
        );
    }
}
