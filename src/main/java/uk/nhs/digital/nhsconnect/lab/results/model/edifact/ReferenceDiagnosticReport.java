package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example RFF+SRI:13/CH001137K/211010191093'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
@AllArgsConstructor
public class ReferenceDiagnosticReport extends Segment {

    public static final String KEY = "RFF";
    public static final String QUALIFIER = "SRI";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    private final String referenceNumber;

    public static ReferenceDiagnosticReport fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + ReferenceDiagnosticReport.class.getSimpleName() + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        String[] valueSplit = Split.byColon(keySplit[1]);

        return new ReferenceDiagnosticReport(valueSplit[1]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return referenceNumber;
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
