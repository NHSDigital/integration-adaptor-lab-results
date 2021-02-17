package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example FTX+CID+++TIRED ALL THE TIME, LOW Hb'
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class ClinicalInformationFreeText extends Segment {
    public static final String KEY = "FTX";
    private static final String QUALIFIER = "CID";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int FREE_TEXT_INDEX = 4;

    private final String clinicalInformationComment;

    public static ClinicalInformationFreeText fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + ClinicalInformationFreeText.class.getSimpleName() + " from " + edifactString);
        }
        String[] split = Split.byPlus(
            Split.bySegmentTerminator(edifactString)[0]
        );
        return new ClinicalInformationFreeText(split[FREE_TEXT_INDEX]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + clinicalInformationComment;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (StringUtils.isBlank(clinicalInformationComment)) {
            throw new EdifactValidationException(KEY + ": Attribute clinicalInformationComment is blank or missing");
        }
    }
}
