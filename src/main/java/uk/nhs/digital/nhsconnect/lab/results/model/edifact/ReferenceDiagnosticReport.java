package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example RFF+SRI:13/CH001137K/211010191093'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class ReferenceDiagnosticReport extends Segment {

    private static final String KEY = "RFF";
    private static final String QUALIFIER = "SRI";
    private static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    @NonNull
    private final String referenceNumber;

    public static ReferenceDiagnosticReport fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + ReferenceDiagnosticReport.class.getSimpleName() + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        final String referenceNumber = Split.byColon(keySplit[1])[1];
        return new ReferenceDiagnosticReport(referenceNumber);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + COLON_SEPARATOR + referenceNumber;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (referenceNumber.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Diagnostic Report Reference is required");
        }
    }
}
