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
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.GpOutboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.MeshOutboundQueueService;

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
    private RecepProducerService recepProducerService;
    @Mock
    private MeshOutboundQueueService meshOutboundQueueService;
    @Mock
    private Interchange interchange;
    @Mock
    private Message message;
    @Mock
    private Message message1;

    @BeforeEach
    void setUp() {
        when(interchange.getInterchangeHeader()).thenReturn(new InterchangeHeader());
    }

    @Test
    void handleInboundMeshMessageNoMessagesDoesNotPublishToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser, times(2)).parse(any());
        verify(edifactToFhirService, never()).convertToFhir(any(Message.class));
        verify(gpOutboundQueueService, never()).publish(any(FhirDataToSend.class));
        verify(recepProducerService).produceRecep(interchange);
        verify(meshOutboundQueueService).publish(any(OutboundMeshMessage.class));
    }

    @Test
    void handleInboundMeshMessageWithMessageAndPublishesToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(List.of(message));

        final Parameters parameters = new Parameters();
        when(edifactToFhirService.convertToFhir(message)).thenReturn(parameters);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser, times(2)).parse(any());
        verify(edifactToFhirService).convertToFhir(message);
        verify(gpOutboundQueueService).publish(any(FhirDataToSend.class));
        verify(recepProducerService).produceRecep(interchange);
        verify(meshOutboundQueueService).publish(any(OutboundMeshMessage.class));
    }

    @Test
    void handleInboundMeshMessageWithMultipleMessagesAndPublishesToGpOutboundQueue() {

        final MeshMessage meshMessage = new MeshMessage();

        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(List.of(message, message1));

        final Parameters parameters = new Parameters();

        when(edifactToFhirService.convertToFhir(message)).thenReturn(parameters);
        when(edifactToFhirService.convertToFhir(message1)).thenReturn(parameters);

        inboundMessageHandler.handle(meshMessage);

        verify(edifactParser, times(2)).parse(any());
        verify(edifactToFhirService).convertToFhir(message);
        verify(edifactToFhirService).convertToFhir(message1);
        verify(gpOutboundQueueService, times(2)).publish(any(FhirDataToSend.class));
        verify(recepProducerService).produceRecep(interchange);
        verify(meshOutboundQueueService).publish(any(OutboundMeshMessage.class));
    }

}
