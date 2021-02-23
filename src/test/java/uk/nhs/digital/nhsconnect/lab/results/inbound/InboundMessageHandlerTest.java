package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessagesParsingException;
import uk.nhs.digital.nhsconnect.lab.results.outbound.OutboundMeshMessageBuilder;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.GpOutboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.outbound.queue.MeshOutboundQueueService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundMessageHandlerTest {

    private static final long SEQUENCE_NUMBER = 123L;

    @InjectMocks
    private InboundMessageHandler inboundMessageHandler;
    @Mock
    private EdifactToFhirService edifactToFhirService;
    @Mock
    private EdifactParser edifactParser;
    @Mock
    private GpOutboundQueueService gpOutboundQueueService;
    @Mock
    private OutboundMeshMessageBuilder outboundMeshMessageBuilder;
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
        lenient().when(interchange.getInterchangeHeader()).thenReturn(
            InterchangeHeader.builder()
                .sender("some_sender")
                .recipient("some_recipient")
                .sequenceNumber(SEQUENCE_NUMBER)
                .translationTime(Instant.now())
                .build());
    }

    @Test
    void handleInvalidInterchangeMeshMessageRaisesException()
            throws InterchangeParsingException, MessagesParsingException {

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY);
        when(edifactParser.parse(meshMessage.getContent())).thenThrow(InterchangeParsingException.class);

        final OutboundMeshMessage outboundMeshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_ACK);
        when(outboundMeshMessageBuilder.buildNhsAck(eq(WorkflowId.PATHOLOGY), any(InterchangeParsingException.class)))
            .thenReturn(outboundMeshMessage);

        inboundMessageHandler.handle(meshMessage);

        assertAll(
            () -> verify(edifactParser).parse(any()),
            () -> verify(edifactToFhirService, never()).convertToFhir(any(Message.class)),
            () -> verify(gpOutboundQueueService, never()).publish(any(Bundle.class)),
            () -> verify(outboundMeshMessageBuilder, never()).buildNhsAck(any(), any(), anyList()),
            () -> verify(meshOutboundQueueService).publish(outboundMeshMessage)
        );
    }

    @Test
    void handleInvalidMessageMeshMessageRaisesException() throws InterchangeParsingException, MessagesParsingException {
        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY);
        when(edifactParser.parse(meshMessage.getContent())).thenThrow(MessagesParsingException.class);

        final OutboundMeshMessage outboundMeshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_ACK);
        when(outboundMeshMessageBuilder.buildNhsAck(eq(WorkflowId.PATHOLOGY), any(MessagesParsingException.class)))
            .thenReturn(outboundMeshMessage);

        inboundMessageHandler.handle(meshMessage);

        assertAll(
            () -> verify(edifactParser).parse(any()),
            () -> verify(edifactToFhirService, never()).convertToFhir(any(Message.class)),
            () -> verify(gpOutboundQueueService, never()).publish(any(Bundle.class)),
            () -> verify(outboundMeshMessageBuilder, never()).buildNhsAck(any(), any(), anyList()),
            () -> verify(meshOutboundQueueService).publish(outboundMeshMessage)
        );
    }

    @Test
    void handleInboundMeshMessageNoMessagesDoesNotPublishToGpOutboundQueue()
            throws InterchangeParsingException, MessagesParsingException {

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY);
        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);

        final OutboundMeshMessage outboundMeshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_ACK);
        when(outboundMeshMessageBuilder
            .buildNhsAck(WorkflowId.PATHOLOGY, interchange, Collections.emptyList()))
            .thenReturn(outboundMeshMessage);

        inboundMessageHandler.handle(meshMessage);

        assertAll(
            () -> verify(edifactParser).parse(any()),
            () -> verify(edifactToFhirService, never()).convertToFhir(any(Message.class)),
            () -> verify(gpOutboundQueueService, never()).publish(any(Bundle.class)),
            () -> verify(outboundMeshMessageBuilder)
                .buildNhsAck(WorkflowId.PATHOLOGY, interchange, Collections.emptyList()),
            () -> verify(meshOutboundQueueService).publish(outboundMeshMessage)
        );
    }

    @Test
    void handleInboundMeshMessageWithMessageAndPublishesToGpOutboundQueue()
            throws InterchangeParsingException, MessagesParsingException {

        final MeshMessage meshMessage = new MeshMessage();
        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(List.of(message));

        final Bundle bundle = new Bundle();
        when(edifactToFhirService.convertToFhir(message)).thenReturn(bundle);

        final OutboundMeshMessage outboundMeshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_ACK);
        when(outboundMeshMessageBuilder.buildNhsAck(any(), eq(interchange), anyList())).thenReturn(outboundMeshMessage);

        inboundMessageHandler.handle(meshMessage);

        assertAll(
            () -> verify(edifactParser).parse(any()),
            () -> verify(edifactToFhirService).convertToFhir(message),
            () -> verify(gpOutboundQueueService).publish(any(Bundle.class)),
            () -> verify(outboundMeshMessageBuilder).buildNhsAck(any(), eq(interchange), anyList()),
            () -> verify(meshOutboundQueueService).publish(outboundMeshMessage)
        );
    }

    @Test
    void handleInboundMeshMessageWithMultipleMessagesAndPublishesToGpOutboundQueue()
            throws InterchangeParsingException, MessagesParsingException {

        final MeshMessage meshMessage = new MeshMessage();
        when(edifactParser.parse(meshMessage.getContent())).thenReturn(interchange);
        when(interchange.getMessages()).thenReturn(List.of(message, message1));

        final Bundle bundle = new Bundle();

        when(edifactToFhirService.convertToFhir(message)).thenReturn(bundle);
        when(edifactToFhirService.convertToFhir(message1)).thenReturn(bundle);

        final OutboundMeshMessage outboundMeshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY_ACK);
        when(outboundMeshMessageBuilder.buildNhsAck(any(), eq(interchange), anyList())).thenReturn(outboundMeshMessage);

        inboundMessageHandler.handle(meshMessage);

        assertAll(
            () -> verify(edifactParser).parse(any()),
            () -> verify(edifactToFhirService).convertToFhir(message),
            () -> verify(edifactToFhirService).convertToFhir(message1),
            () -> verify(gpOutboundQueueService, times(2)).publish(any(Bundle.class)),
            () -> verify(outboundMeshMessageBuilder).buildNhsAck(any(), eq(interchange), anyList()),
            () -> verify(meshOutboundQueueService).publish(outboundMeshMessage)
        );
    }
}
