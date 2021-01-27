package uk.nhs.digital.nhsconnect.lab.results.inbound;


import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.FhirDataToSend;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.GpOutboundQueueService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundMessageHandlerTest {

    @InjectMocks
    private InboundMessageHandler inboundMessageHandler;
    @Mock
    private EdifactToFhirService edifactToFhirService;
    @Mock
    private EdifactParser edifactParser;
    @Mock
    private GpOutboundQueueService gpOutboundQueueService;
    @Mock
    private Interchange interchange;
    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        when(interchange.getInterchangeHeader()).thenReturn(new InterchangeHeader());
    }

    @Test
    void handleInboundMeshMessageNoTransactionsDoesNotPublishToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser).parse(meshMessage.getContent());
        verify(edifactToFhirService, never()).convertToFhir(any(Transaction.class));
        verify(gpOutboundQueueService, never()).publish(any(FhirDataToSend.class));
    }

    @Test
    void handleInboundMeshMessageWithTransactionAndPublishesToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(Collections.singletonList(message));

        final Transaction transaction = new Transaction(new ArrayList<>());
        transaction.setMessage(message);
        when(message.getTransactions()).thenReturn(Collections.singletonList(transaction));
        when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(TransactionType.APPROVAL));

        final Parameters parameters = new Parameters();
        when(edifactToFhirService.convertToFhir(transaction)).thenReturn(parameters);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser).parse(meshMessage.getContent());
        verify(edifactToFhirService).convertToFhir(transaction);
        verify(gpOutboundQueueService).publish(any(FhirDataToSend.class));
    }

    @Test
    void handleInboundMeshMessageWithMultipleTransactionsAndPublishesToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(Collections.singletonList(message));

        final Transaction transaction1 = new Transaction(new ArrayList<>());
        transaction1.setMessage(message);
        final Transaction transaction2 = new Transaction(new ArrayList<>());
        transaction2.setMessage(message);
        when(message.getTransactions()).thenReturn(List.of(transaction1, transaction2));
        when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(TransactionType.APPROVAL));

        final Parameters parameters = new Parameters();

        when(edifactToFhirService.convertToFhir(transaction1)).thenReturn(parameters);
        when(edifactToFhirService.convertToFhir(transaction2)).thenReturn(parameters);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser).parse(meshMessage.getContent());
        verify(edifactToFhirService).convertToFhir(transaction1);
        verify(edifactToFhirService).convertToFhir(transaction2);
        verify(gpOutboundQueueService, times(2)).publish(any(FhirDataToSend.class));
    }

}