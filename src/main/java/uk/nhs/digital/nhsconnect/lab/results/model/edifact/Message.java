package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Message extends Section {

    @Getter(lazy = true)
    private final MessageHeader messageHeader =
        MessageHeader.fromString(extractSegment(MessageHeader.KEY));

    @Getter(lazy = true)
    private final ReferenceTransactionType referenceTransactionType =
        ReferenceTransactionType.fromString(extractSegment(ReferenceTransactionType.KEY_QUALIFIER));

    @Getter
    @Setter
    private Interchange interchange;

    @Getter
    @Setter
    private List<Transaction> transactions;

    public Message(final List<String> edifactSegments) {
        super(edifactSegments);
    }

    @Override
    public String toString() {
        return String.format("Message{SIS: %s, SMS: %s}",
            getInterchange().getInterchangeHeader().getSequenceNumber(),
            getMessageHeader().getSequenceNumber());
    }

}
