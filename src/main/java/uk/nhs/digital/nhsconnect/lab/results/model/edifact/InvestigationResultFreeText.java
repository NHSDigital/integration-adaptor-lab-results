package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Example FTX+RIT+++URINARY STONE weight 13 mg.:Consisted of 36% Calcium oxalate and 64% Calcium phosphate.'
 */
@EqualsAndHashCode(callSuper = true)
public class InvestigationResultFreeText extends FreeTextSegment {
    private static final String QUALIFIER = "RIT";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    public static InvestigationResultFreeText fromString(final String edifact) {
        final String[] texts = FreeTextSegment.extractFreeTextsFromString(edifact, KEY_QUALIFIER,
            InvestigationResultFreeText.class.getSimpleName());
        return new InvestigationResultFreeText(texts);
    }

    public InvestigationResultFreeText(@NonNull final String... texts) {
        super(QUALIFIER, texts);
    }
}
