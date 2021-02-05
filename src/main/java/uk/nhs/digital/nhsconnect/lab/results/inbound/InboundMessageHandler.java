package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.FhirDataToSend;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
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
    private final RecepProducerService recepProducerService;
    private final MeshOutboundQueueService meshOutboundQueueService;

    public void handle(final InboundMeshMessage meshMessage) {

        final Interchange interchange = edifactParser.parse(meshMessage.getContent());

        logInterchangeReceived(interchange);

        final List<Message> messages = interchange.getMessages();

        LOGGER.info("Interchange contains {} new messages", messages.size());

        final List<FhirDataToSend> fhirDataToSend = getFhirDataToSend(messages);

        fhirDataToSend.forEach(gpOutboundQueueService::publish);

        sendRecep(interchange);

        logSentFor(interchange);
    }

    private List<FhirDataToSend> getFhirDataToSend(List<Message> messagesToProcess) {
        return messagesToProcess.stream()
            .map(message -> {
                final Parameters parameters = edifactToFhirService.convertToFhir(message);
                LOGGER.debug("Converted edifact message into {}", parameters);
                return new FhirDataToSend()
                    .setContent(parameters);
            }).collect(Collectors.toList());
    }

    private void logInterchangeReceived(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Translating EDIFACT interchange from Sender={} to Recipient={} with RIS={} containing {} messages",
                interchangeHeader.getSender(), interchangeHeader.getRecipient(), interchangeHeader.getSequenceNumber(),
                interchange.getMessages().size());
        }
    }

    private void sendRecep(final Interchange interchange) {
        final String recepEdifact = recepProducerService.produceRecep(interchange);
        final Interchange recep = edifactParser.parse(recepEdifact);
        final var recepOutboundMessage = prepareRecepOutboundMessage(recepEdifact, recep);

        meshOutboundQueueService.publish(recepOutboundMessage);

        logRecepSentFor(interchange);
    }

    private OutboundMeshMessage prepareRecepOutboundMessage(final String recepEdifact, final Interchange recep) {
        final var recepMeshMessage = buildRecepMeshMessage(recepEdifact, recep);
        LOGGER.debug("Wrapped recep in mesh message: {}", recepMeshMessage);
        return recepMeshMessage;
    }

    private OutboundMeshMessage buildRecepMeshMessage(final String edifactRecep, final Interchange recep) {
        return new MeshMessage()
            .setHaTradingPartnerCode(recep.getInterchangeHeader().getRecipient())
            .setWorkflowId(WorkflowId.RECEP)
            .setContent(edifactRecep);
    }

    private void logSentFor(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Published FHIR for the interchange from Sender={} to Recipient={} with RIS={}",
                interchangeHeader.getSender(), interchangeHeader.getRecipient(), interchangeHeader.getSequenceNumber());
        }
    }

    private void logRecepSentFor(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final var header = interchange.getInterchangeHeader();
            LOGGER.info("Published for async send to MESH a RECEP for the interchange from "
                    + "Sender={} to Recipient={} with RIS={}",
                header.getSender(), header.getRecipient(), header.getSequenceNumber());
        }
    }
}
