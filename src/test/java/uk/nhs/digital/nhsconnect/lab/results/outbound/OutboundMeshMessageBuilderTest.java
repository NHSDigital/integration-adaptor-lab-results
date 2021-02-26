package uk.nhs.digital.nhsconnect.lab.results.outbound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.NhsAckProducerService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;

@ExtendWith(MockitoExtension.class)
class OutboundMeshMessageBuilderTest {

    @InjectMocks
    private OutboundMeshMessageBuilder outboundMeshMessageBuilder;

    @Mock
    private NhsAckProducerService nhsAckProducerService;

    @Test
    void testWhenInterchangeProcessedCorrectlyThenCorrectNhsAckResponseReturned() {
        final var interchange = mock(Interchange.class, RETURNS_DEEP_STUBS);

        when(interchange.getInterchangeHeader().getSender()).thenReturn("Sender");

        when(nhsAckProducerService.createNhsAckEdifact(
                WorkflowId.PATHOLOGY_ACK, interchange, null)).thenReturn("NHSACK");

        OutboundMeshMessage outboundMeshMessage = outboundMeshMessageBuilder.buildNhsAck(
                WorkflowId.PATHOLOGY, interchange, null);

        assertAll(
                () -> assertEquals(WorkflowId.PATHOLOGY_ACK, outboundMeshMessage.getWorkflowId()),
                () -> assertEquals("Sender", outboundMeshMessage.getRecipient()),
                () -> assertEquals("NHSACK", outboundMeshMessage.getContent())
        );
    }

    @Test
    void testWhenScreeningInterchangeProcessesThenCorrectNhsAckResponseReturned() {
        final var interchange = mock(Interchange.class, RETURNS_DEEP_STUBS);

        when(interchange.getInterchangeHeader().getSender()).thenReturn("Sender");

        when(nhsAckProducerService.createNhsAckEdifact(
                WorkflowId.SCREENING_ACK, interchange, null)).thenReturn("NHSACK");

        OutboundMeshMessage outboundMeshMessage = outboundMeshMessageBuilder.buildNhsAck(
                WorkflowId.SCREENING, interchange, null);

        assertAll(
                () -> assertEquals(WorkflowId.SCREENING_ACK, outboundMeshMessage.getWorkflowId()),
                () -> assertEquals("Sender", outboundMeshMessage.getRecipient()),
                () -> assertEquals("NHSACK", outboundMeshMessage.getContent())
        );
    }

    @Test
    void testWhenInterchangeProcessingThrewInterchangeParsingExceptionThenCorrectNhsAckResponseReturned() {
        final var exception = mock(InterchangeParsingException.class);

        when(exception.getSender()).thenReturn("Sender");

        when(nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, exception)).thenReturn("NHSACK");

        OutboundMeshMessage outboundMeshMessage = outboundMeshMessageBuilder.buildNhsAck(
                WorkflowId.PATHOLOGY, exception);

        assertAll(
                () -> assertEquals(WorkflowId.PATHOLOGY_ACK, outboundMeshMessage.getWorkflowId()),
                () -> assertEquals("Sender", outboundMeshMessage.getRecipient()),
                () -> assertEquals("NHSACK", outboundMeshMessage.getContent())
        );
    }

    @Test
    void testWhenInterchangeProcessingThrewMessageParsingExceptionThenCorrectNhsAckResponseReturned() {
        final var exception = mock(MessageParsingException.class);

        when(exception.getSender()).thenReturn("Sender");

        when(nhsAckProducerService.createNhsAckEdifact(WorkflowId.PATHOLOGY_ACK, exception)).thenReturn("NHSACK");

        OutboundMeshMessage outboundMeshMessage = outboundMeshMessageBuilder.buildNhsAck(
                WorkflowId.PATHOLOGY, exception);

        assertAll(
                () -> assertEquals(WorkflowId.PATHOLOGY_ACK, outboundMeshMessage.getWorkflowId()),
                () -> assertEquals("Sender", outboundMeshMessage.getRecipient()),
                () -> assertEquals("NHSACK", outboundMeshMessage.getContent())
        );
    }
}
