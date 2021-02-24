package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    private boolean interchangeError; // This value is only set if an error occurred on the interchange level
    private String interchangeErrorDescription;
    private List<NhsAckMessageContent> messageStatus = new ArrayList<>();
    private int segmentCount;

    public void addMessageProcessingResult(NhsAckMessageContent messageProcessingResult) {
        messageStatus.add(messageProcessingResult);
    }
}
