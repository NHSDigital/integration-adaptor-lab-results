package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NhsAckMessageContent {
    private Long messageNumber;
    private String messageStatus;
    private boolean messageError;
    private String messageErrorDescription;
}
