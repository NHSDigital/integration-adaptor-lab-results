package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import lombok.Getter;

@Getter
public class MessagesParsingException extends Exception {
    private final String sender;
    private final String recipient;
    private final long interchangeSequenceNumber;

    public MessagesParsingException(
            String message, String sender, String recipient, long interchangeSequenceNumber, Exception exception) {

        super(message, exception);
        this.sender = sender;
        this.recipient = recipient;
        this.interchangeSequenceNumber = interchangeSequenceNumber;
    }
}
