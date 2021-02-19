package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Example FTX+SPC+++red blood cell seen, Note low platelets'
 */
@EqualsAndHashCode(callSuper = true)
public class ServiceProviderCommentFreeText extends FreeTextSegment {
    private static final String QUALIFIER = "SPC";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    public static ServiceProviderCommentFreeText fromString(String edifact) {
        final String[] texts = FreeTextSegment.extractFreeTextsFromString(edifact, KEY_QUALIFIER,
            ServiceProviderCommentFreeText.class.getSimpleName());
        return new ServiceProviderCommentFreeText(texts);
    }

    public ServiceProviderCommentFreeText(@NonNull final String... texts) {
        super(QUALIFIER, texts);
    }
}
