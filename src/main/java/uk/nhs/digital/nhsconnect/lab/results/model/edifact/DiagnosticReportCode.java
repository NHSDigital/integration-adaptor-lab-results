package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example GIS+N'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class DiagnosticReportCode extends Segment {

    public static final String KEY = "GIS";

    @NonNull
    private final String code;

    public static DiagnosticReportCode fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportCode.class.getSimpleName()
                + " from " + edifactString);
        }
        final String[] keySplit = Split.byPlus(edifactString);
        final String code = keySplit[1];
        return new DiagnosticReportCode(code);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (code.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Diagnostic Report Code is required");
        }
    }
}
