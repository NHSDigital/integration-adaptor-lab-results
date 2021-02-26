package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import lombok.Getter;

@Getter
public class MessageParsingException extends Exception {
    private final String sender;
    private final String recipient;
    private final long interchangeSequenceNumber;
    private final boolean nhsAckRequested;

    public MessageParsingException(
            String message, String sender, String recipient, long interchangeSequenceNumber,
            boolean nhsAckRequested, Exception exception) {

        super(message, exception);
        this.sender = sender;
        this.recipient = recipient;
        this.interchangeSequenceNumber = interchangeSequenceNumber;
        this.nhsAckRequested = nhsAckRequested;
    }
}
