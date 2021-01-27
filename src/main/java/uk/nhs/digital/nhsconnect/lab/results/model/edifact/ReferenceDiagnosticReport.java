package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

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

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {

    }
}
