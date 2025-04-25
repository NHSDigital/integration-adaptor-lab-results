package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.digital.nhsconnect.lab.results.inbound.ChecksumService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MessageType;
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:MagicNumber")
class GpOutboundQueueServiceTest {

    @InjectMocks
    private GpOutboundQueueService gpOutboundQueueService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectSerializer serializer;

    @Mock
    private CorrelationIdService correlationIdService;

    @Mock
    private ChecksumService checksumService;

    @Mock
    private Session session;

    @Mock
    private TextMessage textMessage;

    @Mock
    private Message message;

    @Mock
    private Interchange interchange;

    @Mock
    private InterchangeHeader interchangeHeader;

    @Mock
    private MessageHeader messageHeader;

    @Captor
    private ArgumentCaptor<MessageCreator> messageCreatorCaptor;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @BeforeEach
    void setUp() {
        lenient().when(message.getInterchange()).thenReturn(interchange);
        lenient().when(interchange.getInterchangeHeader()).thenReturn(interchangeHeader);
        when(message.getMessageHeader()).thenReturn(messageHeader);
    }

    @Test
    void publishMessageToGpOutboundQueue() throws JMSException {
        when(interchangeHeader.getSender()).thenReturn("some_sender");
        when(interchangeHeader.getRecipient()).thenReturn("some_recipient");
        when(interchangeHeader.getSequenceNumber()).thenReturn(123L);
        when(messageHeader.getSequenceNumber()).thenReturn(234L);
        when(messageHeader.getMessageType()).thenReturn(MessageType.PATHOLOGY_VARIANT_3);

        final var bundle = new Bundle();
        final var processingResult = new MessageProcessingResult.Success(message, bundle);

        final String serializedData = "some_serialized_data";

        when(serializer.serialize(bundle)).thenReturn(serializedData);
        when(correlationIdService.getCurrentCorrelationId()).thenReturn("ABC123");

        gpOutboundQueueService.publish(processingResult);

        assertAll(
            () -> verify(serializer).serialize(bundle),
            () -> verify(jmsTemplate).send(eq(gpOutboundQueueName), messageCreatorCaptor.capture())
        );

        when(session.createTextMessage(serializedData)).thenReturn(textMessage);

        messageCreatorCaptor.getValue().createMessage(session);

        assertAll(
            () -> verify(session).createTextMessage(serializedData),
            () -> verify(textMessage).setStringProperty(JmsHeaders.CORRELATION_ID, "ABC123"),
            () -> verify(textMessage).setStringProperty(JmsHeaders.MESSAGE_TYPE, "Pathology"),
            () -> verify(correlationIdService).getCurrentCorrelationId(),
            () -> verify(checksumService)
                .createChecksum("some_sender", "some_recipient", 123L, 234L)
        );
    }

    @Test
    void publishInvalidMessageToGpOutboundQueue() {
        when(messageHeader.getMessageType()).thenReturn(MessageType.NHSACK);
        final var processingResult = new MessageProcessingResult.Success(message, new Bundle());

        assertThatIllegalStateException()
            .isThrownBy(() -> gpOutboundQueueService.publish(processingResult))
            .withMessage("Invalid message type: NHSACK");
    }
}
