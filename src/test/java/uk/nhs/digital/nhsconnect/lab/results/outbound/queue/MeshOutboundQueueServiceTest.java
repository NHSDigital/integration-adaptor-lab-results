package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeshOutboundQueueServiceTest {
    private static final Instant NOW = Instant.now();
    private static final String TIMESTAMP = NOW.toString();

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TimestampService timestampService;
    @Mock
    private MeshClient meshClient;
    @Mock
    private ConversationIdService conversationIdService;

    @Captor
    private ArgumentCaptor<MessageCreator> messageCreatorCaptor;

    @InjectMocks
    private MeshOutboundQueueService meshOutboundQueueService;

    @BeforeEach
    void setUp() {
        meshOutboundQueueService.setMeshOutboundQueueName("queue");
    }

    @Nested
    @DisplayName("publish()")
    class Publish {
        @BeforeEach
        void setUp() {
            when(timestampService.getCurrentTimestamp()).thenReturn(NOW);
            when(timestampService.formatInISO(NOW)).thenReturn(TIMESTAMP);
            when(conversationIdService.getCurrentConversationId()).thenReturn("ConversationID");
        }

        @Test
        @SneakyThrows
        void testPublish() {
            OutboundMeshMessage outboundMeshMessage = mock(OutboundMeshMessage.class);

            meshOutboundQueueService.publish(outboundMeshMessage);

            verify(outboundMeshMessage).setMessageSentTimestamp(TIMESTAMP);
            verify(jmsTemplate).send(eq("queue"), messageCreatorCaptor.capture());

            Session mockSession = mock(Session.class);
            TextMessage mockTextMessage = mock(TextMessage.class);

            when(objectMapper.writeValueAsString(outboundMeshMessage)).thenReturn("Fake Text");
            when(mockSession.createTextMessage("Fake Text")).thenReturn(mockTextMessage);

            Message result = messageCreatorCaptor.getValue().createMessage(mockSession);

            verify(mockTextMessage).setStringProperty(JmsHeaders.CONVERSATION_ID, "ConversationID");
            assertEquals(mockTextMessage, result);
        }
    }

    @Nested
    @DisplayName("receive()")
    class Receive {
        @Test
        @SneakyThrows
        void testReceiveGoodCase() {
            Message message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CONVERSATION_ID)).thenReturn("ConversationID");
            when(message.getBody(String.class)).thenReturn("Message Body");

            OutboundMeshMessage outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class)).thenReturn(outboundMeshMessage);

            meshOutboundQueueService.receive(message);

            verify(conversationIdService).applyConversationId("ConversationID");
            verify(meshClient).authenticate();
            verify(meshClient).sendEdifactMessage(outboundMeshMessage);
            verify(conversationIdService).resetConversationId();
        }

        @Test
        @SneakyThrows
        void testReceiveFailsToApplyConversationId() {
            Message message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CONVERSATION_ID))
                    .thenThrow(new JMSException("Expected exception"));
            when(message.getBody(String.class)).thenReturn("Message Body");

            OutboundMeshMessage outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class)).thenReturn(outboundMeshMessage);

            meshOutboundQueueService.receive(message);

            verify(conversationIdService, never()).applyConversationId("ConversationID");
            verify(meshClient).authenticate();
            verify(meshClient).sendEdifactMessage(outboundMeshMessage);
            verify(conversationIdService).resetConversationId();
        }

        @Test
        @SneakyThrows
        @SuppressWarnings("deprecation")
        void testReceiveFailsToReadBody() {
            Message message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CONVERSATION_ID)).thenReturn("ConversationID");
            when(message.getBody(String.class)).thenReturn("Message Body");

            OutboundMeshMessage outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class))
                    .thenThrow(new JsonMappingException("Expected exception"));

            assertThrows(JsonMappingException.class, () -> meshOutboundQueueService.receive(message));

            verify(conversationIdService).applyConversationId("ConversationID");
            verify(meshClient, never()).authenticate();
            verify(meshClient, never()).sendEdifactMessage(outboundMeshMessage);
            verify(conversationIdService).resetConversationId();
        }
    }
}
