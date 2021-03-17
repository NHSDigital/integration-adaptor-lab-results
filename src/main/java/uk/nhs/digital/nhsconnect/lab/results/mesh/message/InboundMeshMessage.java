package uk.nhs.digital.nhsconnect.lab.results.mesh.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;

public interface InboundMeshMessage {
    WorkflowId getWorkflowId();
    String getContent();
    String getMessageSentTimestamp();
    InboundMeshMessage setMessageSentTimestamp(String timestamp);
    String getMeshMessageId();

    @JsonCreator
    static InboundMeshMessage create(
            @JsonProperty(value = "workflowId") WorkflowId workflowId,
            @JsonProperty(value = "content") String content,
            @JsonProperty(value = "messageSentTimestamp") String messageSentTimestamp,
            @JsonProperty(value = "meshMessageId") String meshMessageId
    ) {
        return new MeshMessage()
                .setWorkflowId(workflowId)
                .setContent(content)
                .setMessageSentTimestamp(messageSentTimestamp)
                .setMeshMessageId(meshMessageId);
    }
}
