package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example RFF+ASL:1'
 */
@Getter
@RequiredArgsConstructor
public class SequenceReference extends Segment {
    public static final String KEY = "RFF";

    @NonNull
    private final SequenceReferenceTarget target;

    @NonNull
    private final String number;

    public static SequenceReference fromString(final String edifact) {
        if (!edifact.startsWith(KEY)) {
            throw new IllegalArgumentException(
                "Can't create " + SequenceReference.class.getSimpleName() + " from " + edifact);
        }
        String[] data = Split.byColon(
            Split.byPlus(edifact)[1]
        );
        return new SequenceReference(SequenceReferenceTarget.fromCode(data[0]), data[1]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return target.getQualifier()
            + COLON_SEPARATOR
            + number;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // no stateful fields to validate
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (!number.matches("\\p{Alnum}{1,6}")) {
            throw new EdifactValidationException(
                KEY + ": attribute number must be an alphanumeric string of up to 6 characters");
        }
    }
}
