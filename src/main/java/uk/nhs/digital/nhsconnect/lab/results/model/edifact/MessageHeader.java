package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MessageType;

/**
 * A specialisation of a segment for the specific use case of a message header.
 * Takes in specific values required to generate a message header.
 * <pre>
 * Example: {@code UNH+1+MEDRPT:0:1:RT:NHS003'}
 * </pre>
 */
@Builder
@Getter
@RequiredArgsConstructor
@Setter
public class MessageHeader extends Segment {

    public static final String KEY = "UNH";
    public static final long MAX_MESSAGE_SEQUENCE = 99_999_999L;

    private final Long sequenceNumber;
    private final MessageType messageType;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static MessageHeader fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + MessageHeader.class.getSimpleName()
                + " from " + edifactString);
        }
        final String[] splitByPlus = Split.byPlus(edifactString);
        final String[] splitByColon = Split.byColon(splitByPlus[2]);

        return MessageHeader.builder()
            .sequenceNumber(Long.valueOf(splitByPlus[1]))
            .messageType(MessageType.fromCode(splitByColon[4]))
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() {
        if (sequenceNumber == null) {
            throw new EdifactValidationException(KEY + ": Attribute sequenceNumber is required");
        }

        if (sequenceNumber < 1 || sequenceNumber > MAX_MESSAGE_SEQUENCE) {
            throw new EdifactValidationException(KEY + ": Attribute sequenceNumber must be between 1 and "
                + MAX_MESSAGE_SEQUENCE);
        }

        if (messageType == null) {
            throw new EdifactValidationException(KEY + ": Attribute messageType is required");
        }
    }
}
