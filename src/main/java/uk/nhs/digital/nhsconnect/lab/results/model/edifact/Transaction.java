package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Transaction extends Section {

    @Getter
    @Setter
    private Message message;

    public Transaction(List<String> segments) {
        super(segments);
    }

    @Override
    public String toString() {
        return String.format("Transaction{SIS: %s, SMS: %s}",
            getMessage().getInterchange().getInterchangeHeader().getSequenceNumber(),
            getMessage().getMessageHeader().getSequenceNumber());
    }
}
