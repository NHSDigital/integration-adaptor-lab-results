package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.inbound.MessageProcessingResult;

@Getter
public class MessageResultException extends RuntimeException {
    private MessageProcessingResult messageProcessingResult;

    public MessageResultException(String message, MessageProcessingResult messageProcessingResult) {
        super(message);
        this.messageProcessingResult = messageProcessingResult;
    }
}
