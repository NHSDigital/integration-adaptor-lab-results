package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

@Getter
@AllArgsConstructor
public class PartnerAgreedIdentification extends Segment {
    private static final String KEY = "RFF";
    private static final String QUALIFIER = "AHI";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    @NonNull
    private final String reference;

    public static PartnerAgreedIdentification fromString(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + PartnerAgreedIdentification.class.getSimpleName()
                + " from " + edifact);
        }

        final String reference = Split.byColon(edifact)[1];
        return new PartnerAgreedIdentification(reference);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            + COLON_SEPARATOR
            + reference;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // no stateful fields to validate
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (reference.isBlank()) {
            throw new EdifactValidationException(KEY + ": Attribute reference is required");
        }
    }
}
