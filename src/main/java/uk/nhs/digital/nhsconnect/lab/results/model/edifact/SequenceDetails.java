package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example SEQ++1'
 */
@Getter
@RequiredArgsConstructor
public class SequenceDetails extends Segment {
    public static final String KEY = "SEQ";

    @NonNull
    private final String number;

    public static SequenceDetails fromString(final String edifact) {
        if (!edifact.startsWith(KEY)) {
            throw new IllegalArgumentException(
                    "Can't create " + SequenceDetails.class.getSimpleName() + " from " + edifact);
        }
        final String sequenceValue = Split.byPlus(edifact)[2];
        return new SequenceDetails(sequenceValue);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (!number.matches("\\p{Alnum}{1,6}")) {
            throw new EdifactValidationException(
                    KEY + ": attribute number must be an alphanumeric string of up to 6 characters");
        }
    }
}
