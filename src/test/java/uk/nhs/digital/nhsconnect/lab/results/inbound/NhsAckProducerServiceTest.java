package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult.Error;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult.Success;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MessageType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NhsAckProducerServiceTest {

    private static final Instant FIXED_TIME = Instant.parse("2020-04-27T16:37:00Z");
    private static final Long FIXED_SEQUENCE_NUMBER = 1000L;
    private static final String NHSACK_IAF_RESPONSE_PATH = "src/test/resources/edifact/nhsAck_example_IAF.txt";
    private static final String NHSACK_IAP_RESPONSE_PATH = "src/test/resources/edifact/nhsAck_example_IAP.txt";
    private static final String NHSACK_IRA_RESPONSE_PATH = "src/test/resources/edifact/nhsAck_example_IRA.txt";
    private static final String NHSACK_IRI_RESPONSE_PATH = "src/test/resources/edifact/nhsAck_example_IRI.txt";
    private static final String NHSACK_IRM_RESPONSE_PATH = "src/test/resources/edifact/nhsAck_example_IRM.txt";
    private static final String NHSACK_SCREENING_RESPONSE_PATH =
        "src/test/resources/edifact/nhsAck_screening_example.txt";

    private static final String INTERCHANGE_SENDER = "000000004400001";
    private static final String INTERCHANGE_RECIPIENT = "000000024600002";
    private static final Long INTERCHANGE_CONTROL_REFERENCE = 1015L;
    private static final Long MESSAGE_SEQUENCE_NUMBER_1 = 1L;
    private static final Long MESSAGE_SEQUENCE_NUMBER_2 = 2L;
    private static final Long MESSAGE_SEQUENCE_NUMBER_3 = 3L;

    @InjectMocks
    private NhsAckProducerService nhsAckProducerService;

    @Mock
    private TimestampService timestampService;

    @Mock
    private SequenceService sequenceService;

    private List<MessageProcessingResult> messageProcessingResults;

    @BeforeEach
    void setUp() {
        when(timestampService.getCurrentTimestamp()).thenReturn(FIXED_TIME);
        when(sequenceService.generateInterchangeSequence(any(), any())).thenReturn(FIXED_SEQUENCE_NUMBER);
        messageProcessingResults = new ArrayList<>();
    }

    @Test
    void testCreateValidNhsAckFromValidInterchangeIAFResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(InterchangeHeader.builder()
            .sender(INTERCHANGE_SENDER)
            .recipient(INTERCHANGE_RECIPIENT)
            .translationTime(FIXED_TIME)
            .sequenceNumber(INTERCHANGE_CONTROL_REFERENCE)
            .build());

        Message message1 = mock(Message.class);
        when(message1.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_1, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message1, null));

        Message message2 = mock(Message.class);
        when(message2.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_2, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message2, null));

        Message message3 = mock(Message.class);
        when(message3.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_3, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message3, null));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.PATHOLOGY_3_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_IAF_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckPartialAcceptanceIAPResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(InterchangeHeader.builder()
            .sender(INTERCHANGE_SENDER)
            .recipient(INTERCHANGE_RECIPIENT)
            .translationTime(FIXED_TIME)
            .sequenceNumber(INTERCHANGE_CONTROL_REFERENCE)
            .build());

        Message message1 = mock(Message.class);
        when(message1.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_1, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message1, null));

        Message message2 = mock(Message.class);
        when(message2.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_2, MessageType.PATHOLOGY_VARIANT_3)
        );
        Exception exception = new Exception("This is a failed message.");
        messageProcessingResults.add(new Error(message2, exception));

        Message message3 = mock(Message.class);
        when(message3.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_3, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message3, null));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.PATHOLOGY_3_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_IAP_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckAllMessagesErrorIRAResponse() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(InterchangeHeader.builder()
            .sender(INTERCHANGE_SENDER)
            .recipient(INTERCHANGE_RECIPIENT)
            .translationTime(FIXED_TIME)
            .sequenceNumber(INTERCHANGE_CONTROL_REFERENCE)
            .build());

        Message message1 = mock(Message.class);
        when(message1.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_1, MessageType.PATHOLOGY_VARIANT_3)
        );
        Exception exception1 = new Exception("This is a failed message.");
        messageProcessingResults.add(new Error(message1, exception1));

        Message message2 = mock(Message.class);
        when(message2.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_2, MessageType.PATHOLOGY_VARIANT_3)
        );
        Exception exception2 = new Exception("This is another failed message.");
        messageProcessingResults.add(new Error(message2, exception2));

        Message message3 = mock(Message.class);
        when(message3.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_3, MessageType.PATHOLOGY_VARIANT_3)
        );
        Exception exception3 = new Exception("This is yet another failed message.");
        messageProcessingResults.add(new Error(message3, exception3));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.PATHOLOGY_3_ACK, interchange, messageProcessingResults);

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

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.PATHOLOGY_3_ACK, interchangeParsingException);

        String expectedNhsAck = readFile(NHSACK_IRI_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckMessageErrorIRMResponse() throws IOException {
        final var messageParsingException = mock(MessageParsingException.class);
        when(messageParsingException.getSender()).thenReturn(INTERCHANGE_SENDER);
        when(messageParsingException.getRecipient()).thenReturn(INTERCHANGE_RECIPIENT);
        when(messageParsingException.getInterchangeSequenceNumber()).thenReturn(INTERCHANGE_CONTROL_REFERENCE);
        when(messageParsingException.getMessage()).thenReturn("Messages could not be extracted.");

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.PATHOLOGY_3_ACK, messageParsingException);

        String expectedNhsAck = readFile(NHSACK_IRM_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    @Test
    void testCreateNhsAckMessageForScreeningReport() throws IOException {
        final var interchange = mock(Interchange.class);
        when(interchange.getInterchangeHeader()).thenReturn(InterchangeHeader.builder()
            .sender(INTERCHANGE_SENDER)
            .recipient(INTERCHANGE_RECIPIENT)
            .translationTime(FIXED_TIME)
            .sequenceNumber(INTERCHANGE_CONTROL_REFERENCE)
            .build());

        Message message1 = mock(Message.class);
        when(message1.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_1, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message1, null));

        Message message2 = mock(Message.class);
        when(message2.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_2, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message2, null));

        Message message3 = mock(Message.class);
        when(message3.getMessageHeader()).thenReturn(
            new MessageHeader(MESSAGE_SEQUENCE_NUMBER_3, MessageType.PATHOLOGY_VARIANT_3)
        );
        messageProcessingResults.add(new Success(message3, null));

        String nhsAck = nhsAckProducerService.createNhsAckEdifact(
            WorkflowId.SCREENING_ACK, interchange, messageProcessingResults);

        String expectedNhsAck = readFile(NHSACK_SCREENING_RESPONSE_PATH).replace("\n", "");

        assertEquals(expectedNhsAck, nhsAck);
    }

    private String readFile(String path) throws IOException {
        Path filePath = FileSystems.getDefault().getPath(path);
        return Files.lines(filePath).collect(Collectors.joining("\n"));
    }
}
