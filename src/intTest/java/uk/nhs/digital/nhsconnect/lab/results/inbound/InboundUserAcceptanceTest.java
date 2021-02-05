package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test EDIFACT message (.dat file) is sent to the MESH mailbox where the adaptor receives inbound messages.
 * The test waits for the messages to be processed and compares the FHIR message published to the GP Outbound Queue
 * with the expected FHIR representation of the original message sent (.json file having the same name as the .dat)
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

        final String content = new String(Files.readAllBytes(getEdifactResource().getFile().toPath()));

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(RECIPIENT,
            WorkflowId.REGISTRATION, content, null, null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        final Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
        assertThat(gpOutboundQueueMessage).isNotNull();

        final String conservationId = gpOutboundQueueMessage.getStringProperty(JmsHeaders.CONVERSATION_ID);
        assertThat(conservationId).isNotEmpty();

        final String expectedMessageBody = new String(Files.readAllBytes(getFhirResource().getFile().toPath()));
        final String messageBody = parseTextMessage(gpOutboundQueueMessage);
        assertThat(messageBody).isEqualTo(expectedMessageBody);

        assertOutboundRecepMessage();
    }

    private void assertOutboundRecepMessage() throws IOException {
        final var labResultMeshClient = getLabResultsMeshClient();
        final var edifactParser = getEdifactParser();
        final var recep = new String(Files.readAllBytes(getRecepResource().getFile().toPath()));

        // Acting as a lab results system, receive and validate the RECEP returned by the adaptor.
        final List<String> messageIds = waitFor(() -> {
            final List<String> inboxMessageIds = labResultMeshClient.getInboxMessageIds();
            return inboxMessageIds.isEmpty() ? null : inboxMessageIds;
        });
        var meshMessage = labResultMeshClient.getEdifactMessage(messageIds.get(0));

        Interchange expectedRecep = edifactParser.parse(recep);
        Interchange actualRecep = edifactParser.parse(meshMessage.getContent());

        assertThat(meshMessage.getWorkflowId())
            .isEqualTo(WorkflowId.RECEP);
        assertThat(actualRecep.getInterchangeHeader().getRecipient())
            .isEqualTo(expectedRecep.getInterchangeHeader().getRecipient());
        assertThat(actualRecep.getInterchangeHeader().getSender())
            .isEqualTo(expectedRecep.getInterchangeHeader().getSender());
        assertThat(actualRecep.getInterchangeHeader().getSequenceNumber())
            .isEqualTo(expectedRecep.getInterchangeHeader().getSequenceNumber());
        assertThat(filterTimestampedSegments(actualRecep))
            .containsExactlyElementsOf(filterTimestampedSegments(expectedRecep));
        assertThat(actualRecep.getInterchangeTrailer().getNumberOfMessages())
            .isEqualTo(expectedRecep.getInterchangeTrailer().getNumberOfMessages());
        assertThat(actualRecep.getInterchangeTrailer().getSequenceNumber())
            .isEqualTo(expectedRecep.getInterchangeTrailer().getSequenceNumber());

    }

    private List<String> filterTimestampedSegments(Interchange recep) {
        final List<String> edifactSegments = recep.getMessages().get(0).getEdifactSegments();
        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment).startsWith("BGM+"));
        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment).startsWith("DTM+815"));

        return edifactSegments.stream()
            .filter(segment -> !segment.startsWith("BGM+"))
            .filter(segment -> !segment.startsWith("DTM+815"))
            .collect(Collectors.toList());
    }
}
