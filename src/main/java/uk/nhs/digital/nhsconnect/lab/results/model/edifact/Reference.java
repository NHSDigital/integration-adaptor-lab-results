package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReferenceType;

/**
 * Example RFF+ASL:1'
 */
@Getter
@RequiredArgsConstructor
public class Reference extends Segment {
    public static final String KEY = "RFF";

    @NonNull
    private final ReferenceType target;

    @NonNull
    private final String number;

    public static Reference fromString(final String edifact) {
        if (!edifact.startsWith(KEY)) {
            throw new IllegalArgumentException(
                "Can't create " + Reference.class.getSimpleName() + " from " + edifact);
        }
        String[] data = Split.byColon(
            Split.byPlus(edifact)[1]
        );
        return new Reference(ReferenceType.fromCode(data[0]), data[1]);
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
