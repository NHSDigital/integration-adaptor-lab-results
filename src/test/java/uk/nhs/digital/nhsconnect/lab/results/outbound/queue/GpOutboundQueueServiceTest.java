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

    @InjectMocks
    private GpOutboundQueueService gpOutboundQueueService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectSerializer serializer;

    @Mock
    private CorrelationIdService correlationIdService;

    @Mock
    private Session session;

    @Mock
    private TextMessage textMessage;

    @Captor
    private ArgumentCaptor<MessageCreator> messageCreatorArgumentCaptor;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @Test
    void publishMessageToGpOutboundQueue() throws JMSException {
        final Bundle bundle = new Bundle();
        final String serializedData = "some_serialized_data";

        when(serializer.serialize(bundle)).thenReturn(serializedData);
        when(correlationIdService.getCurrentCorrelationId()).thenReturn(CONSERVATION_ID);

        gpOutboundQueueService.publish(bundle);

        assertAll(
            () -> verify(serializer).serialize(bundle),
            () -> verify(jmsTemplate).send(eq(gpOutboundQueueName), messageCreatorArgumentCaptor.capture())
        );

        when(session.createTextMessage(serializedData)).thenReturn(textMessage);

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        assertAll(
            () -> verify(session).createTextMessage(eq(serializedData)),
            () -> verify(textMessage).setStringProperty(JmsHeaders.CORRELATION_ID, CONSERVATION_ID),
            () -> verify(correlationIdService).getCurrentCorrelationId()
        );
    }
}
