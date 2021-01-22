package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
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
public class InboundQueueService {

    private final ObjectMapper objectMapper;

    private final TimestampService timestampService;

    private final JmsTemplate jmsTemplate;

    private final ConversationIdService conversationIdService;

    private final InboundMessageHandler inboundMessageHandler;

    @Value("${labresults.amqp.meshInboundQueueName}")
    private String meshInboundQueueName;

    @JmsListener(destination = "${labresults.amqp.meshInboundQueueName}")
    public void receive(final Message message) throws IOException, JMSException {

        try {
            setLoggingConversationId(message);
            final String messageBody = JmsReader.readMessage(message);

            LOGGER.debug("Received message messageBody: {}", messageBody);

            final InboundMeshMessage meshMessage = objectMapper.readValue(messageBody, InboundMeshMessage.class);
            final WorkflowId workflowId = meshMessage.getWorkflowId();

            LOGGER.info("Processing MeshMessageId={} with MeshWorkflowId={}", meshMessage.getMeshMessageId(),
                    workflowId);

            inboundMessageHandler.handle(meshMessage);

            message.acknowledge();
            LOGGER.info("Completed processing MeshMessageId={}", meshMessage.getMeshMessageId());
        } catch (Exception e) {
            LOGGER.error("Error while processing mesh inbound queue message", e);
            throw e;
        } finally {
            conversationIdService.resetConversationId();
        }
    }

    @SneakyThrows
    public void publish(final InboundMeshMessage messageContent) {
        messageContent.setMessageSentTimestamp(timestampService.formatInISO(timestampService.getCurrentTimestamp()));
        jmsTemplate.send(meshInboundQueueName, session -> {
            var message = session.createTextMessage(serializeMeshMessage(messageContent));
            message.setStringProperty(JmsHeaders.CONVERSATION_ID, conversationIdService.getCurrentConversationId());
            return message;
        });
    }

    @SneakyThrows
    private String serializeMeshMessage(final InboundMeshMessage meshMessage) {
        return objectMapper.writeValueAsString(meshMessage);
    }

    private void setLoggingConversationId(final Message message) {
        try {
            final String conversationId = message.getStringProperty(JmsHeaders.CONVERSATION_ID);
            conversationIdService.applyConversationId(conversationId);
        } catch (JMSException e) {
            LOGGER.error("Unable to read header " + JmsHeaders.CONVERSATION_ID + " from message", e);
        }
    }
}
