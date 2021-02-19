package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Example FTX+CID+++TIRED ALL THE TIME, LOW Hb'
 */
@EqualsAndHashCode(callSuper = true)
public class ClinicalInformationFreeText extends FreeTextSegment {
    private static final String QUALIFIER = "CID";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    public static ClinicalInformationFreeText fromString(final String edifact) {
        final String[] texts = FreeTextSegment.extractFreeTextsFromString(edifact, KEY_QUALIFIER,
            ClinicalInformationFreeText.class.getSimpleName());
        return new ClinicalInformationFreeText(texts);
    }

    public ClinicalInformationFreeText(@NonNull final String... text) {
        super(QUALIFIER, text);
    }
}
