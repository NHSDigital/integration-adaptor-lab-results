package uk.nhs.digital.nhsconnect.lab.results.outbound;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler;
import uk.nhs.digital.nhsconnect.lab.results.inbound.NhsackProducerService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessagesParsingException;

import java.util.List;

import static uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId.PATHOLOGY_ACK;
import static uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId.SCREENING_ACK;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Component
public class OutboundMeshMessageBuilder {

    private final NhsackProducerService nhsackProducerService;

    public OutboundMeshMessage buildNhsAck(
            WorkflowId workflowId,
            Interchange interchange,
            List<InboundMessageHandler.MessageProcessingResult> messageProcessingResults) {
        //TODO NIAD-1063: one of IAF, IAP, IRA
        return new MeshMessage()
            .setWorkflowId(getOutboundWorkflowId(workflowId))
            .setRecipient(interchange.getInterchangeHeader().getSender())
            .setContent("TODO NIAD-1063");
    }

    public OutboundMeshMessage buildNhsAck(WorkflowId workflowId, InterchangeParsingException exception) {
        //TODO NIAD-1063
        return new MeshMessage()
            .setWorkflowId(getOutboundWorkflowId(workflowId))
            .setRecipient(exception.getSender())
            .setContent("TODO NIAD-1063");
    }

    public OutboundMeshMessage buildNhsAck(WorkflowId workflowId, MessagesParsingException exception) {
        //TODO NIAD-1063
        return new MeshMessage()
            .setWorkflowId(getOutboundWorkflowId(workflowId))
            .setRecipient(exception.getSender())
            .setContent("TODO NIAD-1063");
    }

    private WorkflowId getOutboundWorkflowId(WorkflowId workflowId) {
        switch (workflowId) {
            case PATHOLOGY:
                return PATHOLOGY_ACK;
            case SCREENING:
                return SCREENING_ACK;
            default:
                throw new IllegalArgumentException(workflowId.name());
        }
    }
}
