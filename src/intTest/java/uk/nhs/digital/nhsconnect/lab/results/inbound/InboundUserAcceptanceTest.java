package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
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
class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "000000024600002";

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
    void testSendEdifactIsProcessedAndPushedToGpOutboundQueue()
        throws JMSException, IOException, InterchangeParsingException, MessageParsingException, JSONException {

        final String content = new String(Files.readAllBytes(getEdifactResource().getFile().toPath()));

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(RECIPIENT,
            WorkflowId.PATHOLOGY, content, null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        final Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
        assertThat(gpOutboundQueueMessage).isNotNull();

        final String correlationId = gpOutboundQueueMessage.getStringProperty(JmsHeaders.CORRELATION_ID);
        assertThat(correlationId).isNotEmpty();

        final String expectedMessageBody = new String(Files.readAllBytes(getFhirResource().getFile().toPath()));
        final String messageBody = parseTextMessage(gpOutboundQueueMessage);

        JSONAssert.assertEquals(
            expectedMessageBody,
            messageBody,
            new CustomComparator(
                JSONCompareMode.STRICT,
                new Customization("id", IGNORE),
                new Customization("meta.lastUpdated", IGNORE),
                new Customization("identifier.value", IGNORE),
                new Customization("entry[*].fullUrl", IGNORE),
                new Customization("entry[*].resource.**.reference", IGNORE),
                new Customization("entry[*].resource.id", IGNORE)
            )
        );

        assertOutboundNhsAckMessage();
    }

    private void assertOutboundNhsAckMessage()
        throws IOException, InterchangeParsingException, MessageParsingException {

        final var labResultMeshClient = getLabResultsMeshClient();
        final var edifactParser = getEdifactParser();
        final var nhsAck = new String(Files.readAllBytes(getNhsAckResource().getFile().toPath()));

        // Acting as a lab results system, receive and validate the NHSACK returned by the adaptor.
        final List<String> messageIds = waitFor(() -> {
            final List<String> inboxMessageIds = labResultMeshClient.getInboxMessageIds();
            return inboxMessageIds.isEmpty() ? null : inboxMessageIds;
        });
        var meshMessage = labResultMeshClient.getEdifactMessage(messageIds.get(0));

        //TODO NIAD-851 UAT framework
//        Interchange expectedNhsAck = edifactParser.parse(nhsAck);
//        Interchange actualNhsAck = edifactParser.parse(meshMessage.getContent());
//
//        assertThat(meshMessage.getWorkflowId())
//            .isEqualTo(WorkflowId.PATHOLOGY_ACK);
//        assertThat(actualNhsAck.getInterchangeHeader().getRecipient())
//            .isEqualTo(expectedNhsAck.getInterchangeHeader().getRecipient());
//        assertThat(actualNhsAck.getInterchangeHeader().getSender())
//            .isEqualTo(expectedNhsAck.getInterchangeHeader().getSender());
//        assertThat(actualNhsAck.getInterchangeHeader().getSequenceNumber())
//            .isEqualTo(expectedNhsAck.getInterchangeHeader().getSequenceNumber());
//        assertThat(filterTimestampedSegments(actualNhsAck))
//            .containsExactlyElementsOf(filterTimestampedSegments(expectedNhsAck));
//        assertThat(actualNhsAck.getInterchangeTrailer().getNumberOfMessages())
//            .isEqualTo(expectedNhsAck.getInterchangeTrailer().getNumberOfMessages());
//        assertThat(actualNhsAck.getInterchangeTrailer().getSequenceNumber())
//            .isEqualTo(expectedNhsAck.getInterchangeTrailer().getSequenceNumber());

    }

    private List<String> filterTimestampedSegments(Interchange nhsAck) {
        final List<String> edifactSegments = nhsAck.getMessages().get(0).getEdifactSegments();
        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment).startsWith("BGM+"));
        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment).startsWith("DTM+815"));

        return edifactSegments.stream()
            .filter(segment -> !segment.startsWith("BGM+"))
            .filter(segment -> !segment.startsWith("DTM+815"))
            .collect(Collectors.toList());
    }
}
