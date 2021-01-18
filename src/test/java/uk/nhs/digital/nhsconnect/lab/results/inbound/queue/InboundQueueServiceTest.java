package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import javax.jms.Session;
import javax.jms.TextMessage;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InboundQueueServiceTest {

    public static final String CONVERSATION_ID = "CONV123";

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private TimestampService timestampService;

    @Mock
    private ConversationIdService conversationIdService;

    @Captor
    private ArgumentCaptor<MessageCreator> jmsMessageCreatorCaptor;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private InboundQueueService inboundQueueService;

    @Test
    public void when_publish_inboundMessageFromMesh_thenTimestampAndConversationIdAreSet() throws Exception {
        final var now = Instant.now();
        when(timestampService.getCurrentTimestamp()).thenReturn(now);
        final var messageSentTimestamp = "2020-06-12T14:15:16Z";
        when(timestampService.formatInISO(now)).thenReturn(messageSentTimestamp);

        InboundMeshMessage inboundMeshMessage = InboundMeshMessage.create(WorkflowId.REGISTRATION, "ASDF", null, "ID123");

        inboundQueueService.publish(inboundMeshMessage);

        // the method parameter is modified so another copy is needed. Timestamp set to expected value
        InboundMeshMessage expectedInboundMeshMessage = InboundMeshMessage.create(WorkflowId.REGISTRATION, "ASDF", messageSentTimestamp, "ID123");
        String expectedStringMessage = objectMapper.writeValueAsString(expectedInboundMeshMessage);
        verify(jmsTemplate).send(org.mockito.Mockito.<String>isNull(), jmsMessageCreatorCaptor.capture());
        MessageCreator messageCreator = jmsMessageCreatorCaptor.getValue();
        Session jmsSession = mock(Session.class);
        TextMessage textMessage = mock(TextMessage.class);
        // should not return a testMessage unless timestamp was set to expected value
        when(jmsSession.createTextMessage(expectedStringMessage)).thenReturn(textMessage);
        when(conversationIdService.getCurrentConversationId()).thenReturn(CONVERSATION_ID);

        var actualTextMessage = messageCreator.createMessage(jmsSession);
        assertThat(actualTextMessage).isSameAs(textMessage);
        verify(textMessage).setStringProperty(JmsHeaders.CONVERSATION_ID, CONVERSATION_ID);
    }

}