package uk.nhs.digital.nhsconnect.lab.results.outbound;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.inbound.NhsAckProducerService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;

import java.util.List;

import static uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId.PATHOLOGY_ACK;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId.SCREENING_ACK;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Component
public class OutboundMeshMessageBuilder {

    private final NhsAckProducerService nhsAckProducerService;

    public OutboundMeshMessage buildNhsAck(
            WorkflowId workflowId,
            Interchange interchange,
            List<MessageProcessingResult> messageProcessingResults) {
        WorkflowId ackWorkflowId = getOutboundWorkflowId(workflowId);
        String nhsAck = nhsAckProducerService.createNhsAckEdifact(interchange, messageProcessingResults);
        return new MeshMessage()
            .setWorkflowId(ackWorkflowId)
            .setRecipient(interchange.getInterchangeHeader().getSender())
            .setContent(nhsAck);
    }

    public OutboundMeshMessage buildNhsAck(WorkflowId workflowId, InterchangeParsingException exception) {
        WorkflowId ackWorkflowId = getOutboundWorkflowId(workflowId);
        String nhsAck = nhsAckProducerService.createNhsAckEdifact(exception);
        return new MeshMessage()
            .setWorkflowId(ackWorkflowId)
            .setRecipient(exception.getSender())
            .setContent(nhsAck);
    }

    public OutboundMeshMessage buildNhsAck(WorkflowId workflowId, MessageParsingException exception) {
        WorkflowId ackWorkflowId = getOutboundWorkflowId(workflowId);
        String nhsAck = nhsAckProducerService.createNhsAckEdifact(exception);
        return new MeshMessage()
            .setWorkflowId(ackWorkflowId)
            .setRecipient(exception.getSender())
            .setContent(nhsAck);
    }

    private WorkflowId getOutboundWorkflowId(WorkflowId workflowId) {
        switch (workflowId) {
            case PATHOLOGY:
                return PATHOLOGY_ACK;
            case SCREENING:
                return SCREENING_ACK;
            default:
                throw new IllegalArgumentException(workflowId.name() + " workflow has no corresponding ACK one");
        }
    }
}
