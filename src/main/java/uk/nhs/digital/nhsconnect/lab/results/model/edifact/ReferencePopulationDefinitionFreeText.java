package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Example: {@code FTX+RPD+++Equivocal'}
 */
@EqualsAndHashCode(callSuper = true)
public class ReferencePopulationDefinitionFreeText extends FreeTextSegment {
    private static final String QUALIFIER = "RPD";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    public static ReferencePopulationDefinitionFreeText fromString(final String edifact) {
        final String[] texts = FreeTextSegment.extractFreeTextsFromString(edifact, KEY_QUALIFIER,
            ReferencePopulationDefinitionFreeText.class.getSimpleName());
        return new ReferencePopulationDefinitionFreeText(texts);
    }

    public ReferencePopulationDefinitionFreeText(@NonNull final String... texts) {
        super(QUALIFIER, texts);
    }
}
