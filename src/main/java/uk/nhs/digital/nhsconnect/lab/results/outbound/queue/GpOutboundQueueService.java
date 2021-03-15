package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.ChecksumService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageType;
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsHeaders;

import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GpOutboundQueueService {
    private static final Map<MessageType, String> MESSAGE_TYPE_HEADER_VALUES = Map.of(
        MessageType.PATHOLOGY, "Pathology",
        MessageType.SCREENING, "Screening"
    );

    private final JmsTemplate jmsTemplate;
    private final ObjectSerializer serializer;
    private final CorrelationIdService correlationIdService;
    private final ChecksumService checksumService;

    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @SneakyThrows
    public void publish(MessageProcessingResult.Success processingResult) {
        final var messageType = MessageType.fromCode(processingResult.getMessage().getMessageHeader()
            .getMessageType());
        final var type = Optional.ofNullable(MESSAGE_TYPE_HEADER_VALUES.get(messageType)).orElseThrow(
            () -> new IllegalStateException("Invalid message type: " + messageType));

        String jsonMessage = serializer.serialize(processingResult.getBundle());

        LOGGER.debug("Encoded FHIR to string: {}", jsonMessage);

        final MessageCreator messageCreator = (Session session) -> {
            final TextMessage message = session.createTextMessage(jsonMessage);
            message.setStringProperty(JmsHeaders.CORRELATION_ID, correlationIdService.getCurrentCorrelationId());
            message.setStringProperty(JmsHeaders.CHECKSUM, buildChecksum(processingResult.getMessage()));
            message.setStringProperty(JmsHeaders.MESSAGE_TYPE, type);
            return message;
        };

        jmsTemplate.send(gpOutboundQueueName, messageCreator);
        LOGGER.debug("Published message to GP Outbound Queue");
    }

    private String buildChecksum(Message message) {
        var interchangeHeader = message.getInterchange().getInterchangeHeader();
        var messageHeader = message.getMessageHeader();

        return checksumService.createChecksum(
            interchangeHeader.getSender(),
            interchangeHeader.getRecipient(),
            interchangeHeader.getSequenceNumber(),
            messageHeader.getSequenceNumber());
    }
}
