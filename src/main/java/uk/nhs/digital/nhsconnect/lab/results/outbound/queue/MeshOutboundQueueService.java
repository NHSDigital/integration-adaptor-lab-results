package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsReader;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MeshOutboundQueueService {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final TimestampService timestampService;
    private final MeshClient meshClient;
    private final ConversationIdService conversationIdService;

    @Value("${labresults.amqp.meshOutboundQueueName}")
    @Setter(AccessLevel.PACKAGE)
    private String meshOutboundQueueName;

    public void publish(OutboundMeshMessage message) {
        LOGGER.info("Publishing message to MESH outbound queue for asynchronous sending to MESH API "
                        + "OperationId={} recipient={} workflow={}",
                message.getOperationId(), message.getHaTradingPartnerCode(), message.getWorkflowId());
        LOGGER.debug("Publishing message content to outbound mesh queue: {}", message);
        message.setMessageSentTimestamp(timestampService.formatInISO(timestampService.getCurrentTimestamp()));
        jmsTemplate.send(meshOutboundQueueName, session -> {
            var textMessage = session.createTextMessage(serializeMeshMessage(message));
            textMessage.setStringProperty(JmsHeaders.CONVERSATION_ID, conversationIdService.getCurrentConversationId());
            return textMessage;
        });
    }

    @SneakyThrows
    private String serializeMeshMessage(OutboundMeshMessage meshMessage) {
        return objectMapper.writeValueAsString(meshMessage);
    }

    @JmsListener(destination = "${labresults.amqp.meshOutboundQueueName}")
    public void receive(Message message) throws IOException, JMSException {
        try {
            setLoggingConversationId(message);
            LOGGER.info("Consuming message from outbound MESH message queue");
            String body = JmsReader.readMessage(message);
            OutboundMeshMessage outboundMeshMessage = objectMapper.readValue(body, OutboundMeshMessage.class);
            LOGGER.debug("Parsed message into object: {}", outboundMeshMessage);
            meshClient.authenticate();
            meshClient.sendEdifactMessage(outboundMeshMessage);
        } finally {
            // Let exceptions throw and be handled by default or configured JMS Listener error handler
            conversationIdService.resetConversationId();
        }
    }

    private void setLoggingConversationId(Message message) {
        try {
            conversationIdService.applyConversationId(message.getStringProperty(JmsHeaders.CONVERSATION_ID));
        } catch (JMSException e) {
            LOGGER.error("Unable to read header " + JmsHeaders.CONVERSATION_ID + " from message", e);
        }
    }
}
