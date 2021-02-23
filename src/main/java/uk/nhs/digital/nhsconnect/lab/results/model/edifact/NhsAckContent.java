package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NhsAckContent {
    private String interchangeSender;
    private String interchangeRecipient;
    private String date_YYMMDD;
    private String time_HHMM;
    private String nhsAckControlReference;
    private String workflowId;
    private String date_CCYYMMDD;
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
