package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Transaction extends Section {
    @Getter(lazy = true)
    private final ReferenceTransactionNumber referenceTransactionNumber =
        ReferenceTransactionNumber.fromString(extractSegment(ReferenceTransactionNumber.KEY_QUALIFIER));

    @Getter
    @Setter
    private Message message;

    public Transaction(List<String> segments) {
        super(segments);
    }

    @Override
    public String toString() {
        return String.format("Transaction{SIS: %s, SMS: %s, TN: %s}",
            getMessage().getInterchange().getInterchangeHeader().getSequenceNumber(),
            getMessage().getMessageHeader().getSequenceNumber(),
            getReferenceTransactionNumber().getTransactionNumber());
    }
}
