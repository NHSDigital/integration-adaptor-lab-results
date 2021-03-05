package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.FailureArgumentsProvider;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.SuccessArgumentsProvider;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.TestData;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId.PATHOLOGY_ACK;
import static uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId.SCREENING_ACK;

class InboundUserAcceptanceTest extends IntegrationBaseTest {

    static final String ACK_REQUESTED_REGEX = "(?s)^.*UNB\\+UNOC.*\\+\\+1'\\s*UNH.*$";

    static final int GP_OUTBOUND_QUEUE_POLLING_DELAY = 2000;
    static final int GP_OUTBOUND_QUEUE_POLLING_TIMEOUT = 10;

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
        clearGpOutboundQueue();
        clearMeshOutboundQueue();
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "true"); //enable scheduling
    }

    @AfterEach
    void tearDown() {
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "false");
    }

    /**
     * The test EDIFACT message (.dat file) is sent to the MESH mailbox where the adaptor receives inbound messages.
     * The test waits for the messages to be processed and compares the FHIR message published to the GP Outbound Queue
     * with the expected FHIR representation of the original message sent (.json file having the same name as the .dat)
     */
    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(SuccessArgumentsProvider.class)
    void testEdifactIsSuccessfullyProcessedAndPushedToGpOutboundQueue(String category, TestData testData)
            throws JMSException, InterchangeParsingException, MessageParsingException, JSONException {

        final String recipient = getEdifactParser().parse(testData.getEdifact())
            .getInterchangeHeader().getRecipient();

        WorkflowId workflowId;
        if (testData.getEdifact().contains(WorkflowId.PATHOLOGY.getWorkflowId())) {
            workflowId = WorkflowId.PATHOLOGY;
        } else if (testData.getEdifact().contains(WorkflowId.SCREENING.getWorkflowId())) {
            workflowId = WorkflowId.SCREENING;
        } else {
            throw new RuntimeException("Unsupported Workflow ID");
        }

        final boolean ackRequested = testData.getEdifact().matches(ACK_REQUESTED_REGEX);

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(recipient,
            workflowId, testData.getEdifact(), null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        final Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
        assertThat(gpOutboundQueueMessage).isNotNull();

        final String correlationId = gpOutboundQueueMessage.getStringProperty(JmsHeaders.CORRELATION_ID);
        assertThat(correlationId).isNotEmpty();

        final String expectedMessageBody = testData.getJson();
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
                new Customization("entry[*].resource.subject.reference", IGNORE),
                new Customization("entry[*].resource.id", IGNORE)
            )
        );

        if (ackRequested) {
            assertOutboundNhsAckMessage(workflowId);
        } else {
            assertThat(getMeshClient().getInboxMessageIds()).isEmpty();
        }

    }

    /**
     * The test EDIFACT message (.dat file) is sent to the MESH mailbox where the adaptor receives inbound messages.
     * The test waits for the messages to be processed confirms that the outbound queue is empty.
     */
    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(FailureArgumentsProvider.class)
    void testEdifactFailsToBeProcessedAndNothingPushedToGpOutboundQueue(String category, TestData testData) {

        String recipient;
        try {
            recipient = getEdifactParser().parse(testData.getEdifact())
                .getInterchangeHeader().getRecipient();
        } catch (InterchangeParsingException e) {
            recipient = e.getRecipient();
        } catch (MessageParsingException e) {
            recipient = e.getRecipient();
        }

        WorkflowId workflowId;
        if (testData.getEdifact().contains(WorkflowId.PATHOLOGY.getWorkflowId())) {
            workflowId = WorkflowId.PATHOLOGY;
        } else if (testData.getEdifact().contains(WorkflowId.SCREENING.getWorkflowId())) {
            workflowId = WorkflowId.SCREENING;
        } else {
            throw new RuntimeException("Unsupported Workflow ID");
        }

        final boolean ackRequested = testData.getEdifact().matches(ACK_REQUESTED_REGEX);

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(recipient,
            workflowId, testData.getEdifact(), null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        await().pollDelay(GP_OUTBOUND_QUEUE_POLLING_DELAY, TimeUnit.MILLISECONDS)
            .atMost(GP_OUTBOUND_QUEUE_POLLING_TIMEOUT, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(gpOutboundQueueIsEmpty()).isTrue());

        if (ackRequested) {
            assertOutboundNhsAckMessage(workflowId);
        } else {
            assertThat(getMeshClient().getInboxMessageIds()).isEmpty();
        }

    }

    private void assertOutboundNhsAckMessage(WorkflowId edifactWorkflowId) {
        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck).isNotNull();

        WorkflowId nhsAckWorkflowID = getNhsAckWorkflowId(edifactWorkflowId);

        assertThat(nhsAck.getWorkflowId()).isEqualTo(nhsAckWorkflowID);
    }

    private WorkflowId getNhsAckWorkflowId(WorkflowId workflowId) {
        switch (workflowId) {
            case PATHOLOGY:
                return PATHOLOGY_ACK;
            case SCREENING:
                return SCREENING_ACK;
            default:
                throw new IllegalArgumentException(workflowId.name() + " workflow has no corresponding ACK one");
        }
    }
}
