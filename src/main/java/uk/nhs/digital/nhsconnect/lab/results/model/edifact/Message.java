package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class Message extends Section {
    private static final String DEFAULT_GP_CODE = "9999";

    @Getter(lazy = true)
    private final MessageHeader messageHeader =
        MessageHeader.fromString(extractSegment(MessageHeader.KEY));

    @Getter(lazy = true)
    private final ReferenceTransactionType referenceTransactionType =
        ReferenceTransactionType.fromString(extractSegment(ReferenceTransactionType.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final HealthAuthorityNameAndAddress healthAuthorityNameAndAddress =
        HealthAuthorityNameAndAddress.fromString(extractSegment(HealthAuthorityNameAndAddress.KEY_QUALIFIER));

    @Getter
    @Setter
    private Interchange interchange;

    @Getter
    @Setter
    private List<Transaction> transactions;

    public Message(final List<String> edifactSegments) {
        super(edifactSegments);
    }

    public String findFirstGpCode() {
        return getTransactions().stream()
            .limit(1)
            .map(transaction -> transaction.extractOptionalSegment(GpNameAndAddress.KEY_QUALIFIER))
            .flatMap(Optional::stream)
            .map(GpNameAndAddress::fromString)
            .map(GpNameAndAddress::getIdentifier)
            .findFirst()
            .orElse(DEFAULT_GP_CODE);
    }

    @Override
    public String toString() {
        return String.format("Message{SIS: %s, SMS: %s}",
            getInterchange().getInterchangeHeader().getSequenceNumber(),
            getMessageHeader().getSequenceNumber());
    }
}
