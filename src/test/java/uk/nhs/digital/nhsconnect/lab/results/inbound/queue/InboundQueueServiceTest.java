package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundQueueServiceTest {

    private static final String CONVERSATION_ID = "CONV123";

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private TimestampService timestampService;

    @Captor
    private ArgumentCaptor<MessageCreator> jmsMessageCreatorCaptor;

    @InjectMocks
    private InboundQueueService inboundQueueService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ConversationIdService conversationIdService;

    @Mock
    private Message message;

    @Mock
    private InboundMessageHandler inboundMessageHandler;

    @Test
    void receiveInboundMessageIsHandledByInboundQueueConsumerService() throws Exception {
        when(message.getStringProperty(JmsHeaders.CONVERSATION_ID)).thenReturn(CONVERSATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"LAB_RESULTS_REG\"}");

        inboundQueueService.receive(message);

        verify(conversationIdService).applyConversationId(CONVERSATION_ID);

        final MeshMessage expectedMeshMessage = new MeshMessage();
        expectedMeshMessage.setWorkflowId(WorkflowId.REGISTRATION);
        verify(inboundMessageHandler).handle(expectedMeshMessage);

        verify(message).acknowledge();
        verify(conversationIdService).resetConversationId();
    }

    @Test
    void receiveInboundMessageWithInvalidWorkflowIdThrowsException() throws Exception {
        when(message.getStringProperty(JmsHeaders.CONVERSATION_ID)).thenReturn(CONVERSATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"INVALID\"}");

        assertThrows(Exception.class, () -> inboundQueueService.receive(message));

        verify(conversationIdService).applyConversationId(CONVERSATION_ID);
        verify(message, never()).acknowledge();
        verify(conversationIdService).resetConversationId();
    }

    @Test
    void receiveInboundMessageHandledByInboundQueueConsumerServiceThrowsException() throws Exception {
        when(message.getStringProperty(JmsHeaders.CONVERSATION_ID)).thenReturn(CONVERSATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"LAB_RESULTS_REG\"}");
        doThrow(RuntimeException.class).when(inboundMessageHandler).handle(any(MeshMessage.class));

        assertThrows(RuntimeException.class, () -> inboundQueueService.receive(message));

        verify(conversationIdService).applyConversationId(CONVERSATION_ID);
        verify(message, never()).acknowledge();
        verify(conversationIdService).resetConversationId();
    }

    @Test
    void receiveInboundMessageSetLoggingConversationHeaderThrowsExceptionCatchesAndContinues() throws Exception {
        doThrow(JMSException.class).when(message).getStringProperty(JmsHeaders.CONVERSATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"LAB_RESULTS_REG\"}");

        inboundQueueService.receive(message);

        verify(conversationIdService, never()).applyConversationId(CONVERSATION_ID);

        final MeshMessage expectedMeshMessage = new MeshMessage();
        expectedMeshMessage.setWorkflowId(WorkflowId.REGISTRATION);
        verify(inboundMessageHandler).handle(expectedMeshMessage);

        verify(message).acknowledge();
        verify(conversationIdService).resetConversationId();
    }

    @Test
    void whenPublishInboundMessageFromMeshThenTimestampAndConversationIdAreSet() throws Exception {
        final var now = Instant.now();
        when(timestampService.getCurrentTimestamp()).thenReturn(now);
        final var messageSentTimestamp = "2020-06-12T14:15:16Z";
        when(timestampService.formatInISO(now)).thenReturn(messageSentTimestamp);

        final InboundMeshMessage inboundMeshMessage = InboundMeshMessage.create(WorkflowId.REGISTRATION, "ASDF", null, "ID123");

        inboundQueueService.publish(inboundMeshMessage);

        // the method parameter is modified so another copy is needed. Timestamp set to expected value
        final InboundMeshMessage expectedInboundMeshMessage = InboundMeshMessage.create(WorkflowId.REGISTRATION, "ASDF", messageSentTimestamp, "ID123");
        final String expectedStringMessage = objectMapper.writeValueAsString(expectedInboundMeshMessage);
        verify(jmsTemplate).send((String) isNull(), jmsMessageCreatorCaptor.capture());
        final MessageCreator messageCreator = jmsMessageCreatorCaptor.getValue();
        final Session jmsSession = mock(Session.class);
        final TextMessage textMessage = mock(TextMessage.class);
        // should not return a testMessage unless timestamp was set to expected value
        when(jmsSession.createTextMessage(expectedStringMessage)).thenReturn(textMessage);
        when(conversationIdService.getCurrentConversationId()).thenReturn(CONVERSATION_ID);

        final var actualTextMessage = messageCreator.createMessage(jmsSession);
        assertEquals(textMessage, actualTextMessage);
        verify(textMessage).setStringProperty(JmsHeaders.CONVERSATION_ID, CONVERSATION_ID);
    }

}
