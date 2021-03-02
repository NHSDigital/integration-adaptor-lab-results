package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * A specialisation of a segment for the specific use case of a message trailer
 * takes in specific values required to generate a message trailer
 * example: UNT+18+00000003'
 */
@Getter
@Setter
@RequiredArgsConstructor
public class MessageTrailer extends Segment {

    public static final String KEY = "UNT";
    private @NonNull Integer numberOfSegments;
    private Long sequenceNumber;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (sequenceNumber == null) {
            throw new EdifactValidationException(KEY + ": Attribute sequenceNumber is required");
        }
        if (sequenceNumber <= 0) {
            throw new EdifactValidationException(KEY
                    + ": Attribute sequenceNumber must be greater than or equal to zero");
        }
        if (numberOfSegments <= 1) {
            throw new EdifactValidationException(KEY
                    + ": Attribute numberOfSegments must be greater than or equal to 2");
        }
    }

    public static MessageTrailer fromString(String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + MessageTrailer.class.getSimpleName()
                    + " from " + edifactString);
        }
        final String[] split = Split.byPlus(edifactString);
        return new MessageTrailer(Integer.parseInt(split[1]))
                .setSequenceNumber(Long.parseLong(split[2]));
    }
}
