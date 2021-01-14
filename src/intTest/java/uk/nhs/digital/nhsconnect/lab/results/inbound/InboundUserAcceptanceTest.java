package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationTestsExtension;
import uk.nhs.digital.nhsconnect.lab.results.mesh.MeshMailBoxScheduler;
import uk.nhs.digital.nhsconnect.lab.results.mesh.RecipientMailboxIdMappings;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHttpClientBuilder;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessageId;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
//import uk.nhs.digital.nhsconnect.nhais.IntegrationBaseTest;
//import uk.nhs.digital.nhsconnect.nhais.IntegrationTestsExtension;
//import uk.nhs.digital.nhsconnect.nhais.mesh.MeshMailBoxScheduler;
//import uk.nhs.digital.nhsconnect.nhais.mesh.message.OutboundMeshMessage;
//import uk.nhs.digital.nhsconnect.nhais.mesh.message.WorkflowId;
//import uk.nhs.digital.nhsconnect.nhais.model.edifact.Interchange;
//import uk.nhs.digital.nhsconnect.nhais.outbound.fhir.FhirParser;
//import uk.nhs.digital.nhsconnect.nhais.uat.common.InboundArgumentsProvider;
//import uk.nhs.digital.nhsconnect.nhais.uat.common.TestData;
//import uk.nhs.digital.nhsconnect.nhais.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads test data from /inbound_uat_data. The EDIFACT .dat files are sent to the MESH mailbox where the adaptor receives
 * inbound transactions. The test waits for the transaction to be processed and compares the FHIR published to the GP
 * System Inbound Queue to the .json file having the name name as the .dat.
 */
//@ExtendWith(IntegrationTestsExtension.class)
//@DirtiesContext
@ExtendWith({SpringExtension.class, IntegrationTestsExtension.class})
@SpringBootTest
@Slf4j
@DirtiesContext
public class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "XX11";
    private static final String CONTENT = "test_message";
    private static final OutboundMeshMessage OUTBOUND_MESH_MESSAGE = OutboundMeshMessage.create(
            RECIPIENT, WorkflowId.REGISTRATION, CONTENT, null, null
    );
//    @Autowired
//    private FhirParser fhirParser;
//
//    @Autowired
//    private EdifactParser edifactParser;


    @Autowired
    private MeshRequests meshRequests;
    @Autowired
    private RecipientMailboxIdMappings recipientMailboxIdMappings;
    @Autowired
    private MeshHttpClientBuilder meshHttpClientBuilder;



    @Autowired
    private MeshMailBoxScheduler meshMailBoxScheduler;

    private String previousConversationId;

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
        clearInboundQueue();
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "true"); //enable scheduling
    }

    @AfterEach
    void tearDown() {
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "false");
    }

    //@ParameterizedTest(name = "[{index}] - {0}")
    //@ArgumentsSource(InboundArgumentsProvider.class)
    @Test
    void testFetchMessageAndSendItToTheInboundQueue() {
//        var recipient = new EdifactParser().parse(testData.getEdifact())
//            .getInterchangeHeader().getRecipient();

        // Acting as an LAB_RESULTS system, send EDIFACT to adaptor's MESH mailbox
        MeshMessageId testMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        InboundMeshMessage meshMessage = labResultsMeshClient.getEdifactMessage(testMessageId.getMessageID());

        assertThat(meshMessage.getContent()).isEqualTo(CONTENT);
        assertThat(meshMessage.getWorkflowId()).isEqualTo(WorkflowId.REGISTRATION);
        System.out.println("yolo: " + meshMessage.getContent());


        Message inboundQueueMessage = getInboundQueueMessage();
    }

//    private Message getGpSystemInboundQueueMessageWithCloseQuarterWorkaround(String category) {
//        if (category.equals("close_quarter_notification/close-quarter-notification")) {
//            // there should be no inbound gp system message for close quarter
//            // fetch without the helper and waitFor since we expect this to be null so only need to try once
//            return jmsTemplate.receive(gpSystemInboundQueueName);
//        } else {
//            // use the helper method that includes a more robust waitFor
//            return getGpSystemInboundQueueMessage();
//        }
//    }

    private void verifyThatCloseQuarterNotificationIsNotPresentOnGpSystemInboundQueue(Message gpSystemInboundQueueMessage) {
        assertThat(gpSystemInboundQueueMessage).isNull();
    }

//    private void verifyThatNonCloseQuarterNotificationMessageIsTranslated(TestData testData, Message gpSystemInboundQueueMessage) throws JMSException {
//        assertThat(gpSystemInboundQueueMessage).isNotNull();
//        // assert transaction type in JMS header is correct
//        assertMessageHeaders(gpSystemInboundQueueMessage, "fp69_prior_notification");
//        // assert output body is correct
//        assertMessageBody(gpSystemInboundQueueMessage, testData.getJson());
//    }
//
//    private void assertMessageBody(Message gpSystemInboundQueueMessage, String expectedBody) throws JMSException {
//        var body = parseTextMessage(gpSystemInboundQueueMessage);
//        assertThat(body).isEqualTo(expectedBody);
//    }
//
//    private void assertMessageHeaders(Message gpSystemInboundQueueMessage, String expectedTransactionType) throws JMSException {
//        String transactionType = gpSystemInboundQueueMessage.getStringProperty(JmsHeaders.TRANSACTION_TYPE);
//        assertThat(transactionType).isEqualTo(expectedTransactionType);
//        String actualConversationId = gpSystemInboundQueueMessage.getStringProperty(JmsHeaders.CONVERSATION_ID);
//        assertThat(actualConversationId).matches("[A-F0-9]{32}");
//        assertThat(actualConversationId).isNotEqualTo(previousConversationId);
//        previousConversationId = actualConversationId;
//    }
//
//    private void assertOutboundRecepMessage(String recep) {
//        // Acting as an NHAIS system, receive and validate the RECEP returned by the adaptor
//        List<String> messageIds = waitFor(() -> {
//            List<String> inboxMessageIds = nhaisMeshClient.getInboxMessageIds();
//            return inboxMessageIds.isEmpty() ? null : inboxMessageIds;
//        } );
//        var meshMessage = nhaisMeshClient.getEdifactMessage(messageIds.get(0));
//
//        Interchange expectedRecep = edifactParser.parse(recep);
//        Interchange actualRecep = edifactParser.parse(meshMessage.getContent());
//
//        assertThat(meshMessage.getWorkflowId()).isEqualTo(WorkflowId.RECEP);
//        assertThat(actualRecep.getInterchangeHeader().getRecipient()).isEqualTo(expectedRecep.getInterchangeHeader().getRecipient());
//        assertThat(actualRecep.getInterchangeHeader().getSender()).isEqualTo(expectedRecep.getInterchangeHeader().getSender());
//        assertThat(actualRecep.getInterchangeHeader().getSequenceNumber()).isEqualTo(expectedRecep.getInterchangeHeader().getSequenceNumber());
//        assertThat(filterTimestampedSegments(actualRecep)).containsExactlyElementsOf(filterTimestampedSegments(expectedRecep));
//        assertThat(actualRecep.getInterchangeTrailer().getNumberOfMessages()).isEqualTo(expectedRecep.getInterchangeTrailer().getNumberOfMessages());
//        assertThat(actualRecep.getInterchangeTrailer().getSequenceNumber()).isEqualTo(expectedRecep.getInterchangeTrailer().getSequenceNumber());
//    }
//
//    private List<String> filterTimestampedSegments(Interchange recep) {
//        List<String> edifactSegments = recep.getMessages().get(0).getEdifactSegments();
//        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment.startsWith("BGM+")));
//        assertThat(edifactSegments).anySatisfy(segment -> assertThat(segment.startsWith("DTM+815")));
//
//        return edifactSegments.stream()
//            .filter(segment -> !segment.startsWith("BGM+"))
//            .filter(segment -> !segment.startsWith("DTM+815"))
//            .collect(Collectors.toList());
//    }
}
