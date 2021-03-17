package uk.nhs.digital.nhsconnect.lab.results.mesh.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;

public interface OutboundMeshMessage {
    String getRecipient();
    WorkflowId getWorkflowId();
    String getContent();
    String getMessageSentTimestamp();
    OutboundMeshMessage setMessageSentTimestamp(String timestamp);

    @JsonCreator
    static OutboundMeshMessage create(
            @JsonProperty(value = "recipient") String recipient,
            @JsonProperty(value = "workflowId") WorkflowId workflowId,
            @JsonProperty(value = "content") String content,
            @JsonProperty(value = "messageSentTimestamp") String messageSentTimestamp
    ) {
        return new MeshMessage()
                .setWorkflowId(workflowId)
                .setContent(content)
                .setMessageSentTimestamp(messageSentTimestamp)
                .setRecipient(recipient);
    }
}
