package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example RFF+SRI:15/CH000037K/200010191704'
 */
@Getter
@Setter
@RequiredArgsConstructor
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
        String[] codeAndCount = Split.byColon(keySplit[1]);

        return new ReferenceDiagnosticReport(codeAndCount[1]);
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
            throw new EdifactValidationException(getKey() + "Diagnostic Report Code is required");
        }
    }
}
