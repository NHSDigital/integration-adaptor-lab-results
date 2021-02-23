package uk.nhs.digital.nhsconnect.lab.results.inbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessagesParsingException;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.MessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.SuccessMessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.ErrorMessageProcessingResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class NhsAckProducerServiceTest {

    private static final Instant FIXED_TIME = Instant.parse("2020-04-27T16:37:00Z");
    private static final String NHSACK_IAF_RESPONSE_PATH = "/edifact/nhsAck_example_IAF.txt";
    private static final String NHSACK_IAP_RESPONSE_PATH = "/edifact/nhsAck_example_IAP.txt";
    private static final String NHSACK_IRA_RESPONSE_PATH = "/edifact/nhsAck_example_IRA.txt";
    private static final String NHSACK_IRI_RESPONSE_PATH = "/edifact/nhsAck_example_IRI.txt";
    private static final String NHSACK_IRM_RESPONSE_PATH = "/edifact/nhsAck_example_IRM.txt";


    private static final String INTERCHANGE_SENDER = "000000004400001:80";
    private static final String INTERCHANGE_RECIPIENT = "000000024600002:80";
    private static final Long INTERCHANGE_CONTROL_REFERENCE = 1015L;

    @InjectMocks
    private NhsAckProducerService nhsAckProducerService;

    @Mock
    private TimestampService timestampService;

    private final EdifactParser edifactParser = new EdifactParser(new InterchangeFactory());

    private List<MessageProcessingResult> messageProcessingResults;

    @BeforeEach
    void setUp() {
        when(timestampService.getCurrentTimestamp()).thenReturn(FIXED_TIME);
        messageProcessingResults = new ArrayList<>();
    }

    @Test
    void testCreateValidNhsAckFromValidInterchangeIAFResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(new InterchangeHeader(
                INTERCHANGE_SENDER, INTERCHANGE_RECIPIENT, FIXED_TIME, INTERCHANGE_CONTROL_REFERENCE, true));

        Message message1 = mock(Message.class);
        when (message1.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(1L));
        messageProcessingResults.add(new SuccessMessageProcessingResult(message1));

        Message message2 = mock(Message.class);
        when (message2.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(2L));
        messageProcessingResults.add(new SuccessMessageProcessingResult(message2));

        Message message3 = mock(Message.class);
        when (message3.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(3L));
        messageProcessingResults.add(new SuccessMessageProcessingResult(message3));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_IAF_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckPartialAcceptanceIAPResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(new InterchangeHeader(
                INTERCHANGE_SENDER, INTERCHANGE_RECIPIENT, FIXED_TIME, INTERCHANGE_CONTROL_REFERENCE, true));

        Message message1 = mock(Message.class);
        when (message1.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(1L));
        messageProcessingResults.add(new SuccessMessageProcessingResult(message1));

        Message message2 = mock(Message.class);
        when (message2.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(2L));
        Exception exception = new Exception("This is a failed message.");
        messageProcessingResults.add(new ErrorMessageProcessingResult(message2, exception));

        Message message3 = mock(Message.class);
        when (message3.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(3L));
        messageProcessingResults.add(new SuccessMessageProcessingResult(message3));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_IAP_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckAllMessagesErrorIRAResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(new InterchangeHeader(
                INTERCHANGE_SENDER, INTERCHANGE_RECIPIENT, FIXED_TIME, INTERCHANGE_CONTROL_REFERENCE, true));

        Message message1 = mock(Message.class);
        when (message1.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(1L));
        Exception exception1 = new Exception("This is a failed message.");
        messageProcessingResults.add(new ErrorMessageProcessingResult(message1, exception1));

        Message message2 = mock(Message.class);
        when (message2.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(2L));
        Exception exception2 = new Exception("This is another failed message.");
        messageProcessingResults.add(new ErrorMessageProcessingResult(message2, exception2));

        Message message3 = mock(Message.class);
        when (message3.getMessageHeader()).thenReturn(new MessageHeader().setSequenceNumber(3L));
        Exception exception3 = new Exception("This is yet another failed message.");
        messageProcessingResults.add(new ErrorMessageProcessingResult(message3, exception3));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_IRA_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckInterchangeErrorIRIResponse() throws IOException {
        final var interchangeParsingException = mock(InterchangeParsingException.class);
        when(interchangeParsingException.getSender()).thenReturn(INTERCHANGE_SENDER);
        when(interchangeParsingException.getRecipient()).thenReturn(INTERCHANGE_RECIPIENT);
        when(interchangeParsingException.getInterchangeSequenceNumber()).thenReturn(INTERCHANGE_CONTROL_REFERENCE);
        when(interchangeParsingException.getMessage()).thenReturn("Interchange could not be processed.");

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, interchangeParsingException);

        String expectedNhsAck = readFile(NHSACK_IRI_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckMessageErrorIRMResponse() throws IOException {
        final var interchangeParsingException = mock(MessagesParsingException.class);
        when(interchangeParsingException.getSender()).thenReturn(INTERCHANGE_SENDER);
        when(interchangeParsingException.getRecipient()).thenReturn(INTERCHANGE_RECIPIENT);
        when(interchangeParsingException.getInterchangeSequenceNumber()).thenReturn(INTERCHANGE_CONTROL_REFERENCE);
        when(interchangeParsingException.getMessage()).thenReturn("Messages could not be extracted.");

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, interchangeParsingException);

        String expectedNhsAck = readFile(NHSACK_IRM_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    private String readFile(String path) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(path)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
