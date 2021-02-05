package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.FhirDataToSend;
import uk.nhs.digital.nhsconnect.lab.results.utils.ConversationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.Session;
import javax.jms.TextMessage;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GpOutboundQueueService {

    private final JmsTemplate jmsTemplate;
    private final ObjectSerializer serializer;
    private final ConversationIdService conversationIdService;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @SneakyThrows
    public void publish(FhirDataToSend fhirDataToSend) {
        String jsonMessage = serializer.serialize(fhirDataToSend.getContent());

        LOGGER.debug("Encoded FHIR to string: {}", jsonMessage);

        final MessageCreator messageCreator = (Session session) -> {
            final TextMessage message = session.createTextMessage(jsonMessage);
            message.setStringProperty(JmsHeaders.OPERATION_ID, fhirDataToSend.getOperationId());
            message.setStringProperty(JmsHeaders.CONVERSATION_ID, conversationIdService.getCurrentConversationId());
            return message;
        };

        jmsTemplate.send(gpOutboundQueueName, messageCreator);
        LOGGER.debug("Published message to GP Outbound Queue");
    }

}
