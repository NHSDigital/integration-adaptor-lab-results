package uk.nhs.digital.nhsconnect.lab.results.inbound;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.*;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessagesParsingException;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.MessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.SuccessMessageProcessingResult;
import uk.nhs.digital.nhsconnect.lab.results.inbound.InboundMessageHandler.ErrorMessageProcessingResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.loadTemplate;
import static uk.nhs.digital.nhsconnect.lab.results.utils.TemplateUtils.fillTemplate;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NhsAckProducerService {

    private final TimestampService timestampService;

    private static final int COMMON_SEGMENT_COUNT = 5;

    public String createNhsAckEdifact(WorkflowId workflowId, Interchange interchange,
                                      List<MessageProcessingResult> messageProcessingResults) {
        Mustache nhsAckTemplate = loadTemplate("nhsAck.mustache");

        NhsAckContent nhsAckContent = new NhsAckContent();

        InterchangeHeader interchangeHeader = interchange.getInterchangeHeader();

        nhsAckContent.setInterchangeSender(interchangeHeader.getSender());
        nhsAckContent.setInterchangeRecipient(interchangeHeader.getRecipient());
        nhsAckContent.setInterchangeControlReference(String.valueOf(interchangeHeader.getSequenceNumber()));
        nhsAckContent.setNhsAckControlReference(String.valueOf(interchangeHeader.getSequenceNumber()));

        setDateTimes(nhsAckContent);

        nhsAckContent.setWorkflowId(workflowId.getWorkflowId());

        int acceptedMessages = 0;
        int rejectedMessages = 0;
        int totalMessages = messageProcessingResults.size();

        for (MessageProcessingResult messageProcessingResult : messageProcessingResults) {
            NhsAckMessageContent nhsAckMessageContent = new NhsAckMessageContent();
            if (messageProcessingResult instanceof SuccessMessageProcessingResult) {
                SuccessMessageProcessingResult currentMessageResult = (SuccessMessageProcessingResult) messageProcessingResult;
                MessageHeader messageHeader = currentMessageResult.getMessage().getMessageHeader();
                nhsAckMessageContent.setMessageNumber(messageHeader.getSequenceNumber());
                nhsAckMessageContent.setMessageStatus("MA");
                acceptedMessages++;
            } else {
                ErrorMessageProcessingResult currentMessageResult = (ErrorMessageProcessingResult) messageProcessingResult;
                MessageHeader messageHeader = currentMessageResult.getMessage().getMessageHeader();
                nhsAckMessageContent.setMessageNumber(messageHeader.getSequenceNumber());
                nhsAckMessageContent.setMessageStatus("MR");
                rejectedMessages++;

                nhsAckMessageContent.setMessageError(true);
                nhsAckMessageContent.setMessageErrorDescription(currentMessageResult.getException().getMessage());
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

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    public String createNhsAckEdifact(WorkflowId workflowId, InterchangeParsingException exception) {
        Mustache nhsAckTemplate = loadTemplate("nhsAck.mustache");

        NhsAckContent nhsAckContent = new NhsAckContent();

        nhsAckContent.setInterchangeSender(exception.getSender());
        nhsAckContent.setInterchangeRecipient(exception.getRecipient());
        nhsAckContent.setInterchangeControlReference(String.valueOf(exception.getInterchangeSequenceNumber()));
        nhsAckContent.setNhsAckControlReference(String.valueOf(exception.getInterchangeSequenceNumber()));

        setDateTimes(nhsAckContent);

        nhsAckContent.setWorkflowId(workflowId.getWorkflowId());

        nhsAckContent.setInterchangeStatusCode(NhsAckStatus.INTERCHANGE_REJECTED);

        nhsAckContent.setInterchangeError(true);
        nhsAckContent.setInterchangeErrorDescription(exception.getMessage());

        nhsAckContent.setSegmentCount(COMMON_SEGMENT_COUNT + 1);

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    public String createNhsAckEdifact(WorkflowId workflowId, MessagesParsingException exception) {
        Mustache nhsAckTemplate = loadTemplate("nhsAck.mustache");

        NhsAckContent nhsAckContent = new NhsAckContent();

        nhsAckContent.setInterchangeSender(exception.getSender());
        nhsAckContent.setInterchangeRecipient(exception.getRecipient());
        nhsAckContent.setInterchangeControlReference(String.valueOf(exception.getInterchangeSequenceNumber()));
        nhsAckContent.setNhsAckControlReference(String.valueOf(exception.getInterchangeSequenceNumber()));

        setDateTimes(nhsAckContent);

        nhsAckContent.setWorkflowId(workflowId.getWorkflowId());

        nhsAckContent.setInterchangeStatusCode(NhsAckStatus.MESSAGE_REJECTED);

        nhsAckContent.setInterchangeError(true);
        nhsAckContent.setInterchangeErrorDescription(exception.getMessage());

        nhsAckContent.setSegmentCount(COMMON_SEGMENT_COUNT + 1);

        String segments = fillTemplate(nhsAckTemplate, nhsAckContent);

        return segments.replace("\n", "");
    }

    private void setDateTimes(NhsAckContent nhsAckContent) {
        Instant nhsAckGenerationTime = timestampService.getCurrentTimestamp();
        ZoneId timezone = TimestampService.UK_ZONE;

        nhsAckContent.setDate_CCYYMMDD(DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(timezone).format(nhsAckGenerationTime));
        nhsAckContent.setDate_YYMMDD(DateTimeFormatter.ofPattern("yyMMdd")
                .withZone(timezone).format(nhsAckGenerationTime));
        nhsAckContent.setTime_HHMM(DateTimeFormatter.ofPattern("HHmm")
                .withZone(timezone).format(nhsAckGenerationTime));
    }
}