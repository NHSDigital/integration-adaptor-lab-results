package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RecepHeader extends InterchangeHeader {

    public RecepHeader(
        final @NonNull String sender,
        final @NonNull String recipient,
        final @NonNull Instant translationTime
    ) {
        super(sender, recipient, translationTime);
    }

    public RecepHeader setSequenceNumber(final long sequenceNumber) {
        return (RecepHeader) super.setSequenceNumber(sequenceNumber);
    }

    @Override
    public String getValue() {
        return super.getValue() + "++RECEP+++EDIFACT TRANSFER";
    }
}
