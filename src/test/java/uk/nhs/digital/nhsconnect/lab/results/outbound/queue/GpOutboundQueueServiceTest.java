package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Inbound;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

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
    private ConversationIdService conversationIdService;
    @Mock
    private Session session;
    @Mock
    private TextMessage textMessage;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @Test
    void publishMessageToGpOutboundQueue() throws JMSException {
        final Parameters parameters = new Parameters();

        final DataToSend dataToSend = new DataToSend()
                .setOperationId("123")
                .setTransactionType(Inbound.APPROVAL)
                .setContent(parameters);

        final String serializedData = "some_serialized_data";

        when(serializer.serialize(parameters)).thenReturn(serializedData);
        when(conversationIdService.getCurrentConversationId()).thenReturn(CONSERVATION_ID);

        gpOutboundQueueService.publish(dataToSend);

        verify(serializer).serialize(dataToSend.getContent());

        final ArgumentCaptor<MessageCreator> messageCreatorArgumentCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        verify(jmsTemplate).send(eq(gpOutboundQueueName), messageCreatorArgumentCaptor.capture());

        when(session.createTextMessage(serializedData)).thenReturn(textMessage);

        messageCreatorArgumentCaptor.getValue().createMessage(session);

        verify(session).createTextMessage(eq(serializedData));
        verify(textMessage).setStringProperty(JmsHeaders.OPERATION_ID, dataToSend.getOperationId());
        verify(textMessage).setStringProperty(JmsHeaders.TRANSACTION_TYPE, dataToSend.getTransactionType().name().toLowerCase());
        verify(textMessage).setStringProperty(JmsHeaders.CONVERSATION_ID, CONSERVATION_ID);

        verify(conversationIdService).getCurrentConversationId();
    }

}
