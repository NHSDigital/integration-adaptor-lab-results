package uk.nhs.digital.nhsconnect.lab.results.outbound;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.ObjectSerializer;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OutboundQueueService {

    private final JmsTemplate jmsTemplate;
    private final ObjectSerializer serializer;
    private final ConversationIdService conversationIdService;

    @Value("${labresults.amqp.meshOutboundQueueName}")
    private String meshOutboundQueueName;

    @SneakyThrows
    public void publish(DataToSend dataToSend) {
        String jsonMessage = serializer.serialize(dataToSend.getContent());
        LOGGER.debug("Encoded FHIR to string: {}", jsonMessage);
        jmsTemplate.send(meshOutboundQueueName, session -> {
            var message = session.createTextMessage(jsonMessage);
            message.setStringProperty(JmsHeaders.OPERATION_ID, dataToSend.getOperationId());
            message.setStringProperty(JmsHeaders.TRANSACTION_TYPE, dataToSend.getTransactionType().name().toLowerCase());
            message.setStringProperty(JmsHeaders.CONVERSATION_ID, conversationIdService.getCurrentConversationId());
            return message;
        });
        LOGGER.debug("Published message to outbound queue");
    }

}
