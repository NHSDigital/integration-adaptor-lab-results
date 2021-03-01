package uk.nhs.digital.nhsconnect.lab.results.inbound;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.NhsAckContent;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.NhsAckStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.NhsAckMessageContent;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult.Success;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult.Error;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.loadTemplate;
import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.fillTemplate;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NhsAckProducerService {

    @Autowired
    private final TimestampService timestampService;

    @Autowired
    private final SequenceService sequenceService;

    private static final int COMMON_SEGMENT_COUNT = 5;

    private Mustache nhsAckTemplate = loadTemplate("nhsAck.mustache");

    public String createNhsAckEdifact(WorkflowId workflowId, Interchange interchange,
                                      List<MessageProcessingResult> messageProcessingResults) {

        InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();

        var nhsAckContent = NhsAckContent.builder()
            .interchangeSender(interchangeHeader.getSender())
            .interchangeRecipient(interchangeHeader.getRecipient())
            .interchangeControlReference(String.valueOf(interchangeHeader.getSequenceNumber()))
            .nhsAckControlReference(String.valueOf(sequenceService.generateInterchangeSequence(
                interchangeHeader.getRecipient(), interchangeHeader.getSender())))
            .workflowId(workflowId.getWorkflowId())
            .messageStatus(new ArrayList<>()).build();

        int acceptedMessages = 0;
        int rejectedMessages = 0;
        int totalMessages = messageProcessingResults.size();

        final String MESSAGE_ACCEPTED_STATUS_CODE = "MA";
        final String MESSAGE_REJECTED_STATUS_CODE = "MR";

        for (MessageProcessingResult messageProcessingResult : messageProcessingResults) {
            NhsAckMessageContent nhsAckMessageContent;
            if (messageProcessingResult instanceof MessageProcessingResult.Success) {
                Success currentMessageResult = (Success) messageProcessingResult;
                MessageHeader messageHeader = currentMessageResult.getMessage().getMessageHeader();
                nhsAckMessageContent = NhsAckMessageContent.builder()
                        .messageNumber(messageHeader.getSequenceNumber())
                        .messageStatus(MESSAGE_ACCEPTED_STATUS_CODE)
                        .build();
                acceptedMessages++;
            } else {
                Error currentMessageResult = (Error) messageProcessingResult;
                MessageHeader messageHeader = currentMessageResult.getMessage().getMessageHeader();
                nhsAckMessageContent = NhsAckMessageContent.builder()
                        .messageNumber(messageHeader.getSequenceNumber())
                        .messageStatus(MESSAGE_REJECTED_STATUS_CODE)
                        .messageError(true)
                        .messageErrorDescription(currentMessageResult.getException().getMessage())
                        .build();
                rejectedMessages++;
            }
            nhsAckContent.addMessageProcessingResult(nhsAckMessageContent);
        }

        if (acceptedMessages == totalMessages) {
            nhsAckContent.setInterchangeStatusCode(NhsAckStatus.ACCEPTED);
        } else if (rejectedMessages == totalMessages) {
            nhsAckContent.setInterchangeStatusCode(NhsAckStatus.ALL_MESSAGES_REJECTED);
        } else {
            nhsAckContent.setInterchangeStatusCode(NhsAckStatus.PARTIALLY_ACCEPTED);
        }

        nhsAckContent.setSegmentCount(COMMON_SEGMENT_COUNT + acceptedMessages + (2 * rejectedMessages));

        setDateTimes(nhsAckContent);

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    public String createNhsAckEdifact(WorkflowId workflowId, InterchangeParsingException exception) {
        var nhsAckContent = NhsAckContent.builder()
                .interchangeSender(exception.getSender())
                .interchangeRecipient(exception.getRecipient())
                .interchangeControlReference(String.valueOf(exception.getInterchangeSequenceNumber()))
                .nhsAckControlReference(String.valueOf(sequenceService.generateInterchangeSequence(
                    exception.getRecipient(), exception.getSender())))
                .workflowId(workflowId.getWorkflowId())
                .interchangeStatusCode(NhsAckStatus.INTERCHANGE_REJECTED)
                .interchangeError(true)
                .interchangeErrorDescription(exception.getMessage())
                .segmentCount(COMMON_SEGMENT_COUNT + 1)
                .build();

        setDateTimes(nhsAckContent);

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    public String createNhsAckEdifact(WorkflowId workflowId, MessageParsingException exception) {
        var nhsAckContent = NhsAckContent.builder()
            .interchangeSender(exception.getSender())
            .interchangeRecipient(exception.getRecipient())
            .interchangeControlReference(String.valueOf(exception.getInterchangeSequenceNumber()))
            .nhsAckControlReference(String.valueOf(sequenceService.generateInterchangeSequence(
                exception.getRecipient(), exception.getSender())))
            .workflowId(workflowId.getWorkflowId())
            .interchangeStatusCode(NhsAckStatus.MESSAGE_REJECTED)
            .interchangeError(true)
            .interchangeErrorDescription(exception.getMessage())
            .segmentCount(COMMON_SEGMENT_COUNT + 1)
            .build();

        setDateTimes(nhsAckContent);

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    private void setDateTimes(NhsAckContent nhsAckContent) {
        Instant nhsAckGenerationTime = timestampService.getCurrentTimestamp();
        ZoneId timezone = TimestampService.UK_ZONE;

        nhsAckContent.setDateCCYYMMDD(DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(timezone).format(nhsAckGenerationTime));
        nhsAckContent.setDateYYMMDD(DateTimeFormatter.ofPattern("yyMMdd")
                .withZone(timezone).format(nhsAckGenerationTime));
        nhsAckContent.setTimeHHMM(DateTimeFormatter.ofPattern("HHmm")
                .withZone(timezone).format(nhsAckGenerationTime));
    }
}