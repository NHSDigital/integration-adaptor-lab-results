package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example CIN+UN'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class ClinicalInformationCode extends Segment {
    public static final String KEY = "CIN";

    @NonNull
    private final String code;

    public static ClinicalInformationCode fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + ClinicalInformationCode.class.getSimpleName()
                + " from " + edifactString);
        }
        final String[] keySplit = Split.byPlus(edifactString);
        final String code = keySplit[1];
        return new ClinicalInformationCode(code);
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
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (code.isBlank()) {
            throw new EdifactValidationException(KEY + ": Clinical Information Code is required");
        }
    }
}
