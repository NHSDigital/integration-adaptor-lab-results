package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class NhsAckContent {
    private String interchangeSender;
    private String interchangeRecipient;
    private String dateYYMMDD;
    private String timeHHMM;
    private String nhsAckControlReference;
    private String workflowId;
    private String dateCCYYMMDD;
    private String interchangeControlReference;
    private NhsAckStatus interchangeStatusCode;
    private boolean interchangeError;
    private String interchangeErrorDescription;
    private List<NhsAckMessageContent> messageStatus;
    private int segmentCount;

    public void addMessageProcessingResult(NhsAckMessageContent messageProcessingResult) {
        messageStatus.add(messageProcessingResult);
    }
}
