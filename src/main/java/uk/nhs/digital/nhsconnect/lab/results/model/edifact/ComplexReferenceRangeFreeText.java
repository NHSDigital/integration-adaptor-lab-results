package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Example FTX+CRR+++DVT Prophylaxis - INR 2.0-2.5:Treatment of DVT,PE,AF,TIA - INR 2.0-3.0'
 */
@EqualsAndHashCode(callSuper = true)
public class ComplexReferenceRangeFreeText extends FreeTextSegment {
    private static final String QUALIFIER = "CRR";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    public static ComplexReferenceRangeFreeText fromString(final String edifact) {
        final String[] texts = FreeTextSegment.extractFreeTextsFromString(edifact, KEY_QUALIFIER,
            ComplexReferenceRangeFreeText.class.getSimpleName());
        return new ComplexReferenceRangeFreeText(texts);
    }

    public ComplexReferenceRangeFreeText(@NonNull final String... texts) {
        super(QUALIFIER, texts);
    }
}
