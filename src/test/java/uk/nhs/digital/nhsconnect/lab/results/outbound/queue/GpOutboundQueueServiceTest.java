package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import org.hl7.fhir.dstu3.model.Bundle;
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
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GpOutboundQueueServiceTest {

    private static final String CONSERVATION_ID = "ABC123";
    private static final String SENDER = "some_sender";
    private static final String RECIPIENT = "some_recipient";
    private static final long INTERCHANGE_SEQUENCE_NUMBER = 123;
    private static final long MESSAGE_SEQUENCE_NUMBER = 234;

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
    private ArgumentCaptor<MessageCreator> messageCreatorArgumentCaptor;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @Test
    void publishMessageToGpOutboundQueue() throws JMSException {
        when(message.getInterchange()).thenReturn(interchange);
        when(interchange.getInterchangeHeader()).thenReturn(interchangeHeader);
        when(message.getMessageHeader()).thenReturn(messageHeader);
        when(interchangeHeader.getSender()).thenReturn(SENDER);
        when(interchangeHeader.getRecipient()).thenReturn(RECIPIENT);
        when(interchangeHeader.getSequenceNumber()).thenReturn(INTERCHANGE_SEQUENCE_NUMBER);
        when(messageHeader.getSequenceNumber()).thenReturn(MESSAGE_SEQUENCE_NUMBER);

        final var bundle = new Bundle();
        final var processingResult = new MessageProcessingResult.Success(message, bundle);

        final String serializedData = "some_serialized_data";

        when(serializer.serialize(bundle)).thenReturn(serializedData);
        when(correlationIdService.getCurrentCorrelationId()).thenReturn(CONSERVATION_ID);

        gpOutboundQueueService.publish(processingResult);

        assertAll(
            () -> verify(serializer).serialize(bundle),
            () -> verify(jmsTemplate).send(eq(gpOutboundQueueName), messageCreatorArgumentCaptor.capture())
        );

        when(session.createTextMessage(serializedData)).thenReturn(textMessage);

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        assertAll(
            () -> verify(session).createTextMessage(eq(serializedData)),
            () -> verify(textMessage).setStringProperty(JmsHeaders.CORRELATION_ID, CONSERVATION_ID),
            () -> verify(correlationIdService).getCurrentCorrelationId(),
            () -> verify(checksumService)
                .createChecksum(SENDER, RECIPIENT, INTERCHANGE_SEQUENCE_NUMBER, MESSAGE_SEQUENCE_NUMBER)
        );
    }
}
