package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * A specialisation of a segment for the specific use case of a message header
 * takes in specific values required to generate an message header
 * example: UNH+00000003+FHSREG:0:1:FH:FHS001'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader extends Segment {

    public static final String KEY = "UNH";
    public static final long MAX_MESSAGE_SEQUENCE = 99_999_999L;

    private Long sequenceNumber;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() {
        if (sequenceNumber == null) {
            throw new EdifactValidationException(getKey() + ": Attribute sequenceNumber is required");
        }
        if (sequenceNumber < 1 || sequenceNumber > MAX_MESSAGE_SEQUENCE) {
            throw new EdifactValidationException(getKey() + ": Attribute sequenceNumber must be between 1 and "
                + MAX_MESSAGE_SEQUENCE);
        }
    }

    public static MessageHeader fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + MessageHeader.class.getSimpleName()
                + " from " + edifactString);
        }
        final String[] split = Split.byPlus(edifactString);
        return new MessageHeader(Long.valueOf(split[1]));
    }
}
