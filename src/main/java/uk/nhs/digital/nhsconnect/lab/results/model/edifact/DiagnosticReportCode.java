package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DiagnosticReportCode extends Segment {

    public static final String KEY = "GIS";
    private final @NonNull String code;

    public static DiagnosticReportCode fromString(String edifactString) {
        if (!edifactString.startsWith(DiagnosticReportCode.KEY)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportCode.class.getSimpleName() + " from " + edifactString);
        }
        String[] keySplit = Split.byPlus(edifactString);
        return new DiagnosticReportCode(keySplit[1]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return code;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        if (code.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Diagnostic Report Code is required");

        }
    }

    @Override
    public void preValidate() throws EdifactValidationException {

    }
}
