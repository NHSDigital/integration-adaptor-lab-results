package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InboundQueueService {

    private final ObjectMapper objectMapper;

    private final TimestampService timestampService;

    private final JmsTemplate jmsTemplate;

    private final ConversationIdService conversationIdService;

    @Value("${labresults.amqp.meshInboundQueueName}")
    private String meshInboundQueueName;

    @SneakyThrows
    public void publish(InboundMeshMessage messageContent) {
        messageContent.setMessageSentTimestamp(timestampService.formatInISO(timestampService.getCurrentTimestamp()));
        jmsTemplate.send(meshInboundQueueName, session -> {
            var message = session.createTextMessage(serializeMeshMessage(messageContent));
            message.setStringProperty(JmsHeaders.conversationId, conversationIdService.getCurrentConversationId());
            return message;
        });
    }

    @SneakyThrows
    private String serializeMeshMessage(InboundMeshMessage meshMessage) {
        return objectMapper.writeValueAsString(meshMessage);
    }
}
