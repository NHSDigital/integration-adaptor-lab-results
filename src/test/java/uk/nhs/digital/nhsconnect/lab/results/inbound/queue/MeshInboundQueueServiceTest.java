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
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;
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
class MeshInboundQueueServiceTest {

    private static final String CORRELATION_ID = "CORR123";
    private static final String PATHOLOGY_WORKFLOW_ID = WorkflowId.PATHOLOGY.getWorkflowId();
    private static final String SCREENING_WORKFLOW_ID = WorkflowId.SCREENING.getWorkflowId();

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private TimestampService timestampService;

    @Captor
    private ArgumentCaptor<MessageCreator> jmsMessageCreatorCaptor;

    @InjectMocks
    private MeshInboundQueueService meshInboundQueueService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private CorrelationIdService correlationIdService;

    @Mock
    private Message message;

    @Mock
    private InboundMessageHandler inboundMessageHandler;

    @Test
    void receiveInboundMessageIsHandledByInboundQueueConsumerService() throws Exception {
        when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn(CORRELATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"" + PATHOLOGY_WORKFLOW_ID + "\"}");

        meshInboundQueueService.receive(message);

        verify(correlationIdService).applyCorrelationId(CORRELATION_ID);

        final MeshMessage expectedMeshMessage = new MeshMessage();
        expectedMeshMessage.setWorkflowId(WorkflowId.PATHOLOGY);
        verify(inboundMessageHandler).handle(expectedMeshMessage);

        verify(message).acknowledge();
        verify(correlationIdService).resetCorrelationId();
    }

    @Test
    void receiveInboundMessageWithInvalidWorkflowIdThrowsException() throws Exception {
        when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn(CORRELATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"INVALID\"}");

        assertThrows(Exception.class, () -> meshInboundQueueService.receive(message));

        verify(correlationIdService).applyCorrelationId(CORRELATION_ID);
        verify(message, never()).acknowledge();
        verify(correlationIdService).resetCorrelationId();
    }

    @Test
    void receiveInboundMessageHandledByInboundQueueConsumerServiceThrowsException() throws Exception {
        when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn(CORRELATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"" + PATHOLOGY_WORKFLOW_ID + "\"}");
        doThrow(RuntimeException.class).when(inboundMessageHandler).handle(any(MeshMessage.class));

        assertThrows(RuntimeException.class, () -> meshInboundQueueService.receive(message));

        verify(correlationIdService).applyCorrelationId(CORRELATION_ID);
        verify(message, never()).acknowledge();
        verify(correlationIdService).resetCorrelationId();
    }

    @Test
    void receiveInboundMessageForUnsupportedWorkflowIdThrowsException() throws Exception {
        when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn(CORRELATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"" + SCREENING_WORKFLOW_ID + "\"}");

        final UnknownWorkflowException exception =
            assertThrows(UnknownWorkflowException.class, () -> meshInboundQueueService.receive(message));

        assertEquals("Unknown workflow id: " + SCREENING_WORKFLOW_ID, exception.getMessage());

        verify(correlationIdService).applyCorrelationId(CORRELATION_ID);
        verify(message, never()).acknowledge();
        verify(correlationIdService).resetCorrelationId();
    }

    @Test
    void receiveInboundMessageSetLoggingCorrelationHeaderThrowsExceptionCatchesAndContinues() throws Exception {
        doThrow(JMSException.class).when(message).getStringProperty(JmsHeaders.CORRELATION_ID);
        when(message.getBody(String.class)).thenReturn("{\"workflowId\":\"" + PATHOLOGY_WORKFLOW_ID + "\"}");

        meshInboundQueueService.receive(message);

        verify(correlationIdService, never()).applyCorrelationId(CORRELATION_ID);

        final MeshMessage expectedMeshMessage = new MeshMessage();
        expectedMeshMessage.setWorkflowId(WorkflowId.PATHOLOGY);
        verify(inboundMessageHandler).handle(expectedMeshMessage);

        verify(message).acknowledge();
        verify(correlationIdService).resetCorrelationId();
    }

    @Test
    void whenPublishInboundMessageFromMeshThenTimestampAndCorrelationIdAreSet() throws Exception {
        final var now = Instant.now();
        when(timestampService.getCurrentTimestamp()).thenReturn(now);
        final var messageSentTimestamp = "2020-06-12T14:15:16Z";
        when(timestampService.formatInISO(now)).thenReturn(messageSentTimestamp);

        final InboundMeshMessage inboundMeshMessage = InboundMeshMessage.create(WorkflowId.PATHOLOGY,
            "ASDF", null, "ID123");

        meshInboundQueueService.publish(inboundMeshMessage);

        // the method parameter is modified so another copy is needed. Timestamp set to expected value
        final InboundMeshMessage expectedInboundMeshMessage = InboundMeshMessage.create(WorkflowId.PATHOLOGY,
            "ASDF", messageSentTimestamp,
            "ID123");
        final String expectedStringMessage = objectMapper.writeValueAsString(expectedInboundMeshMessage);
        verify(jmsTemplate).send((String) isNull(), jmsMessageCreatorCaptor.capture());
        final MessageCreator messageCreator = jmsMessageCreatorCaptor.getValue();
        final Session jmsSession = mock(Session.class);
        final TextMessage textMessage = mock(TextMessage.class);
        // should not return a testMessage unless timestamp was set to expected value
        when(jmsSession.createTextMessage(expectedStringMessage)).thenReturn(textMessage);
        when(correlationIdService.getCurrentCorrelationId()).thenReturn(CORRELATION_ID);

        final var actualTextMessage = messageCreator.createMessage(jmsSession);
        assertEquals(textMessage, actualTextMessage);
        verify(textMessage).setStringProperty(JmsHeaders.CORRELATION_ID, CORRELATION_ID);
    }

}
