package uk.nhs.digital.nhsconnect.lab.results.mesh.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MeshMessageId {
    private final String messageID;

    @JsonCreator
    public MeshMessageId(@JsonProperty("messageId") String messageID) {
        this.messageID = messageID;
    }
}
