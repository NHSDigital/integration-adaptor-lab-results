package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Message extends Section {
    private static final String DEFAULT_GP_CODE = "9999";

    @Getter(lazy = true)
    private final MessageHeader messageHeader =
        MessageHeader.fromString(extractSegment(MessageHeader.KEY));

    @Getter(lazy = true)
    private final HealthAuthorityNameAndAddress healthAuthorityNameAndAddress =
        HealthAuthorityNameAndAddress.fromString(extractSegment(HealthAuthorityNameAndAddress.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final Optional<RequesterNameAndAddress> requesterNameAndAddress =
        extractOptionalSegment(RequesterNameAndAddress.KEY_QUALIFIER)
            .map(RequesterNameAndAddress::fromString);

    @Getter(lazy = true)
    private final MessageTrailer messageTrailer =
        MessageTrailer.fromString(extractSegment(MessageTrailer.KEY));

    @Getter
    @Setter
    private Interchange interchange;

    public Message(final List<String> edifactSegments) {
        super(edifactSegments);
    }

    public String findFirstGpCode() {
        return extractOptionalSegment(GpNameAndAddress.KEY_QUALIFIER)
            .stream()
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
