package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.outbound.OutboundMeshMessageBuilder;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.GpOutboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.MeshOutboundQueueService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InboundMessageHandler {

    private final EdifactToFhirService edifactToFhirService;
    private final EdifactParser edifactParser;
    private final GpOutboundQueueService gpOutboundQueueService;
    private final MeshOutboundQueueService meshOutboundQueueService;
    private final OutboundMeshMessageBuilder outboundMeshMessageBuilder;

    public void handle(final InboundMeshMessage meshMessage) {
        final Interchange interchange;
        try {
            interchange = edifactParser.parse(meshMessage.getContent());
        } catch (InterchangeParsingException ex) {
            LOGGER.error("Error parsing Interchange", ex);
            if (ex.isNhsAckRequested()) {
                var nhsack = outboundMeshMessageBuilder.buildNhsAck(meshMessage.getWorkflowId(), ex);
                meshOutboundQueueService.publish(nhsack);
            } else {
                LOGGER.info("NHSACK not requested for interchange: " + ex.getInterchangeSequenceNumber());
            }
            return;
        } catch (MessageParsingException ex) {
            LOGGER.error("Error parsing Messages", ex);
            if (ex.isNhsAckRequested()) {
                var nhsack = outboundMeshMessageBuilder.buildNhsAck(meshMessage.getWorkflowId(), ex);
                meshOutboundQueueService.publish(nhsack);
            } else {
                LOGGER.info("NHSACK not requested for interchange: " + ex.getInterchangeSequenceNumber());
            }
            return;
        }

        logInterchangeReceived(interchange);

        final List<MessageProcessingResult> messageProcessingResults = getFhirDataToSend(interchange.getMessages());

        boolean ackRequested = interchange.getInterchangeHeader().isNhsAckRequested();

        OutboundMeshMessage nhsack = null;

        if (ackRequested) {
            nhsack = outboundMeshMessageBuilder.buildNhsAck(
                meshMessage.getWorkflowId(),
                interchange,
                messageProcessingResults);
        } else {
            LOGGER.info("NHSACK not requested for interchange: "
                + interchange.getInterchangeHeader().getSequenceNumber());
        }

        messageProcessingResults.stream()
            .filter(MessageProcessingResult.Success.class::isInstance)
            .map(MessageProcessingResult.Success.class::cast)
            .forEach(gpOutboundQueueService::publish);

        logSentFor(interchange);

        if (ackRequested) {
            meshOutboundQueueService.publish(nhsack);
            logNhsackSentFor(interchange);
        }
    }

    private List<MessageProcessingResult> getFhirDataToSend(List<Message> messages) {
        return messages.stream()
            .map(this::convertToFhir)
            .collect(Collectors.toList());
    }

    private MessageProcessingResult convertToFhir(Message message) {
        try {
            final var bundle = edifactToFhirService.convertToFhir(message);
            LOGGER.debug("Converted edifact message into {}", bundle);
            return new MessageProcessingResult.Success(message, bundle);
        } catch (Exception ex) {
            LOGGER.error("Error converting Message to FHIR", ex);
            return new MessageProcessingResult.Error(message, ex);
        }
    }

    private void logInterchangeReceived(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Translating EDIFACT interchange from Sender={} to Recipient={} with RIS={} "
                    + "containing {} messages", interchangeHeader.getSender(), interchangeHeader.getRecipient(),
                interchangeHeader.getSequenceNumber(), interchange.getMessages().size());
        }
    }

    private void logSentFor(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Published FHIR for the interchange from Sender={} to Recipient={} with RIS={}",
                interchangeHeader.getSender(), interchangeHeader.getRecipient(),
                interchangeHeader.getSequenceNumber());
        }
    }

    private void logNhsackSentFor(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final var header = interchange.getInterchangeHeader();
            LOGGER.info("Published for async send to MESH an NHSACK for the interchange from "
                    + "Sender={} to Recipient={} with RIS={}",
                header.getSender(), header.getRecipient(), header.getSequenceNumber());
        }
    }
}
