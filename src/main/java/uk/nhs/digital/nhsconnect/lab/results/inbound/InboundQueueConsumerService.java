package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.GpOutboundQueueService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InboundQueueConsumerService {

    private final InboundEdifactTransactionHandler inboundEdifactTransactionService;
    private final EdifactParser edifactParser;
    private final GpOutboundQueueService gpOutboundQueueService;

    public void handle(final InboundMeshMessage meshMessage) {

        final Interchange interchange = edifactParser.parse(meshMessage.getContent());

        logInterchangeReceived(interchange);

        final List<Transaction> transactionsToProcess = interchange.getMessages().stream()
                .map(Message::getTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        LOGGER.info("Interchange contains {} new transactions", transactionsToProcess.size());

        final List<DataToSend> gpOutboundQueueFhirDataToSend = transactionsToProcess.stream()
                .map(transaction -> {
                    final DataToSend fhirDataToSend = inboundEdifactTransactionService.translate(transaction);
                    LOGGER.debug("Converted edifact message into {}", fhirDataToSend.getContent());
                    return fhirDataToSend.setTransactionType(transaction.getMessage().getReferenceTransactionType().getTransactionType());
                }).collect(Collectors.toList());

        gpOutboundQueueFhirDataToSend.forEach(gpOutboundQueueService::publish);

        logSentFor(interchange);
    }

    private void logInterchangeReceived(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Translating EDIFACT interchange from Sender={} to Recipient={} with RIS={} containing {} messages",
                    interchangeHeader.getSender(), interchangeHeader.getRecipient(), interchangeHeader.getSequenceNumber(),
                    interchange.getMessages().size());
        }
    }

    private void logSentFor(final Interchange interchange) {
        if (LOGGER.isInfoEnabled()) {
            final InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();
            LOGGER.info("Published FHIR for the interchange from Sender={} to Recipient={} with RIS={}",
                    interchangeHeader.getSender(), interchangeHeader.getRecipient(), interchangeHeader.getSequenceNumber());
        }
    }

}
