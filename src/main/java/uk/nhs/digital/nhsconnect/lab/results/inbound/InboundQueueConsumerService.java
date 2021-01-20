package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.outbound.OutboundQueueService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboundQueueConsumerService {

    private final InboundEdifactTransactionHandler inboundEdifactTransactionService;
    private final EdifactParser edifactParser;
    private final OutboundQueueService outboundQueueService;

    public void handle(final InboundMeshMessage meshMessage) {

        final Interchange interchange = edifactParser.parse(meshMessage.getContent());

        final List<Transaction> transactionsToProcess = interchange.getMessages().stream()
                .map(Message::getTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        LOGGER.info("Interchange contains {} new transactions", transactionsToProcess.size());

        final List<DataToSend> outboundQueueFhirDataToSend = transactionsToProcess.stream()
                .map(transaction -> {
                    final DataToSend fhirDataToSend = inboundEdifactTransactionService.translate(transaction);
                    return fhirDataToSend.setTransactionType(transaction.getMessage().getReferenceTransactionType().getTransactionType());
                }).collect(Collectors.toList());

        outboundQueueFhirDataToSend.forEach(outboundQueueService::publish);
    }

}
