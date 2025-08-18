package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.FailureArgumentsProvider;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.SuccessArgumentsProvider;
import uk.nhs.digital.nhsconnect.lab.results.uat.common.TestData;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import jakarta.jms.Message;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId.PATHOLOGY_2_ACK;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId.PATHOLOGY_3_ACK;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId.SCREENING_ACK;

class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String ACK_REQUESTED_REGEX = "(?s)^.*UNB\\+(UNOC|UNOB).*\\+\\+1'\\s*UNH.*$";

    private static final int GP_OUTBOUND_QUEUE_POLLING_DELAY = 2000;
    private static final int GP_OUTBOUND_QUEUE_POLLING_TIMEOUT = 10000;

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
     * The test EDIFACT message (.edifact.dat file) is sent to the MESH mailbox where the adaptor receives inbound
     * messages. The test waits for the messages to be processed and compares the FHIR message published to the
     * GP Outbound Queue with the expected FHIR representation of the original message sent
     * (.[messageNumber].fhir.json file having the same name as the .dat). The json files must be numbered in the same
     * order as the messages in the EDIFACT.
     * <p>
     * E.g: testMessage.edifact.dat, testMessage.1.fhir.json, testMessage.2.fhir.json
     */
    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(SuccessArgumentsProvider.class)
    void testEdifactIsSuccessfullyProcessedAndPushedToGpOutboundQueue(String testGroupName, TestData testData)
        throws InterchangeParsingException, MessageParsingException {

        final String edifact = testData.getEdifact();

        final InterchangeHeader interchangeHeader = getEdifactParser().parse(edifact).getInterchangeHeader();

        final String recipient = interchangeHeader.getRecipient();

        final String sender = interchangeHeader.getSender();

        final WorkflowId workflowId = getEdifactWorkflowId(edifact);

        final boolean ackRequested = edifact.matches(ACK_REQUESTED_REGEX);

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(recipient,
            workflowId, edifact, null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        var assertions = testData.getJsonList().stream()
            .map(expectedMessageBody -> {
                Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
                return Pair.of(expectedMessageBody, gpOutboundQueueMessage);
            })
            .map(pair -> (Executable) () -> {
                var expectedMessageBody = pair.getKey();
                var jmsMessage = pair.getValue();
                assertThat(jmsMessage).isNotNull();

                String correlationId = jmsMessage.getStringProperty(JmsHeaders.CORRELATION_ID);
                assertThat(correlationId).isNotEmpty();

                String messageBody = parseTextMessage(jmsMessage);
                assertFhirEquals(expectedMessageBody, messageBody);
            })
            .toArray(Executable[]::new);

        assertAll(assertions);

        if (ackRequested) {
            assertOutboundNhsAckMessage(workflowId, recipient, sender);
        } else {
            assertThat(getMeshClient().getInboxMessageIds()).isEmpty();
        }
    }

    /**
     * The test EDIFACT message (.edifact.dat file) is sent to the MESH mailbox where the adaptor receives inbound
     * messages. The test waits for the messages to be processed and confirms that the outbound queue is empty.
     */
    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(FailureArgumentsProvider.class)
    void testEdifactFailsToBeProcessedAndNothingPushedToGpOutboundQueue(String testGroupName, TestData testData)
        throws InterchangeParsingException, MessageParsingException {

        final String edifact = testData.getEdifact();

        String recipient;
        String sender;
        try {
            InterchangeHeader interchangeHeader = getEdifactParser().parse(edifact).getInterchangeHeader();
            recipient = interchangeHeader.getRecipient();
            sender = interchangeHeader.getSender();
        } catch (InterchangeParsingException e) {
            recipient = e.getRecipient();
            sender = e.getSender();
        } catch (MessageParsingException e) {
            recipient = e.getRecipient();
            sender = e.getSender();
        }

        final WorkflowId workflowId = getEdifactWorkflowId(edifact);

        final boolean ackRequested = edifact.matches(ACK_REQUESTED_REGEX);

        final OutboundMeshMessage outboundMeshMessage = OutboundMeshMessage.create(recipient,
            workflowId, edifact, null);

        getLabResultsMeshClient().sendEdifactMessage(outboundMeshMessage);

        await().pollDelay(GP_OUTBOUND_QUEUE_POLLING_DELAY, TimeUnit.MILLISECONDS)
            .atMost(GP_OUTBOUND_QUEUE_POLLING_TIMEOUT, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(gpOutboundQueueIsEmpty()).isTrue());

        if (ackRequested) {
            assertOutboundNhsAckMessage(workflowId, recipient, sender);
        } else {
            assertThat(getMeshClient().getInboxMessageIds()).isEmpty();
        }

    }

    private void assertOutboundNhsAckMessage(WorkflowId edifactWorkflowId,
                                             String edifactRecipient,
                                             String edifactSender)
        throws InterchangeParsingException, MessageParsingException {
        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck).isNotNull();

        WorkflowId nhsAckWorkflowID = getNhsAckWorkflowId(edifactWorkflowId);

        assertThat(nhsAck.getWorkflowId()).isEqualTo(nhsAckWorkflowID);

        InterchangeHeader nhsAckInterchangeHeader = getEdifactParser().parse(nhsAck.getContent())
            .getInterchangeHeader();

        String nhsAckSender = nhsAckInterchangeHeader.getSender();

        String nhsAckRecipient = nhsAckInterchangeHeader.getRecipient();

        assertThat(nhsAckRecipient).isEqualTo(edifactSender);

        assertThat(nhsAckSender).isEqualTo(edifactRecipient);
    }

    private WorkflowId getEdifactWorkflowId(String edifact) {
        if (edifact.contains("NHS002")) {
            return WorkflowId.PATHOLOGY_2;
        }
        if (edifact.contains("NHS003")) {
            return WorkflowId.PATHOLOGY_3;
        }
        if (edifact.contains("NHS004")) {
            return WorkflowId.SCREENING;
        }
        throw new RuntimeException("Unsupported Workflow ID");
    }

    private WorkflowId getNhsAckWorkflowId(WorkflowId workflowId) {
        switch (workflowId) {
            case PATHOLOGY_2:
                return PATHOLOGY_2_ACK;
            case PATHOLOGY_3:
                return PATHOLOGY_3_ACK;
            case SCREENING:
                return SCREENING_ACK;
            default:
                throw new IllegalArgumentException(workflowId.name() + " workflow has no corresponding ACK one");
        }
    }
}
