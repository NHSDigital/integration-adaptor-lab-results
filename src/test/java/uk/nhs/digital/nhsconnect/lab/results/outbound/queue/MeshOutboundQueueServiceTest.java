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
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
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
    private CorrelationIdService correlationIdService;

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
            when(correlationIdService.getCurrentCorrelationId()).thenReturn("CorrelationID");
        }

        @Test
        @SneakyThrows
        void testPublish() {
            final var outboundMeshMessage = mock(OutboundMeshMessage.class);

            meshOutboundQueueService.publish(outboundMeshMessage);

            assertAll(
                    () -> verify(outboundMeshMessage).setMessageSentTimestamp(TIMESTAMP),
                    () -> verify(jmsTemplate).send(eq("queue"), messageCreatorCaptor.capture())
            );

            final var mockSession = mock(Session.class);
            final var mockTextMessage = mock(TextMessage.class);

            when(objectMapper.writeValueAsString(outboundMeshMessage)).thenReturn("Fake Text");
            when(mockSession.createTextMessage("Fake Text")).thenReturn(mockTextMessage);

            final Message result = messageCreatorCaptor.getValue().createMessage(mockSession);

            assertAll(
                    () -> verify(mockTextMessage).setStringProperty(JmsHeaders.CORRELATION_ID, "CorrelationID"),
                    () -> assertEquals(mockTextMessage, result)
            );
        }
    }

    @Nested
    @DisplayName("receive()")
    class Receive {
        @Test
        @SneakyThrows
        void testReceiveGoodCase() {
            final var message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn("CorrelationID");
            when(message.getBody(String.class)).thenReturn("Message Body");

            final var outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class)).thenReturn(outboundMeshMessage);

            meshOutboundQueueService.receive(message);

            assertAll(
                    () -> verify(correlationIdService).applyCorrelationId("CorrelationID"),
                    () -> verify(meshClient).authenticate(),
                    () -> verify(meshClient).sendEdifactMessage(outboundMeshMessage),
                    () -> verify(correlationIdService).resetCorrelationId()
            );
        }

        @Test
        @SneakyThrows
        void testReceiveFailsToApplyCorrelationId() {
            final var message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CORRELATION_ID))
                    .thenThrow(new JMSException("Expected exception"));
            when(message.getBody(String.class)).thenReturn("Message Body");

            final var outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class)).thenReturn(outboundMeshMessage);

            meshOutboundQueueService.receive(message);

            assertAll(
                    () -> verify(correlationIdService, never()).applyCorrelationId("CorrelationID"),
                    () -> verify(meshClient).authenticate(),
                    () -> verify(meshClient).sendEdifactMessage(outboundMeshMessage),
                    () -> verify(correlationIdService).resetCorrelationId()
            );
        }

        @Test
        @SneakyThrows
        @SuppressWarnings("deprecation")
        void testReceiveFailsToReadBody() {
            final var message = mock(Message.class);
            when(message.getStringProperty(JmsHeaders.CORRELATION_ID)).thenReturn("CorrelationID");
            when(message.getBody(String.class)).thenReturn("Message Body");

            final var outboundMeshMessage = mock(OutboundMeshMessage.class);
            when(objectMapper.readValue("Message Body", OutboundMeshMessage.class))
                    .thenThrow(new JsonMappingException("Expected exception"));

            assertAll(
                    () -> assertThrows(JsonMappingException.class, () -> meshOutboundQueueService.receive(message)),
                    () -> verify(correlationIdService).applyCorrelationId("CorrelationID"),
                    () -> verify(meshClient, never()).authenticate(),
                    () -> verify(meshClient, never()).sendEdifactMessage(outboundMeshMessage),
                    () -> verify(correlationIdService).resetCorrelationId()
            );
        }
    }
}
