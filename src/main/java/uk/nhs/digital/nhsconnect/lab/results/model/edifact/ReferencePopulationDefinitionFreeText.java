package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Arrays;

/**
 * Example: {@code FTX+RPD+++Equivocal'}
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
public class ReferencePopulationDefinitionFreeText extends Segment {
    private static final String KEY = "FTX";
    private static final String QUALIFIER = "RPD";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private static final int INDEX_FREE_TEXTS = 4;
    private static final int MAXIMUM_FREE_TEXTS = 5;

    @NonNull
    private String[] freeTexts;

    public static ReferencePopulationDefinitionFreeText fromText(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create "
                + ReferencePopulationDefinitionFreeText.class.getSimpleName() + " from " + edifact);
        }

        final String[] split = Split.byPlus(edifact);
        final String freeTextComponent = split[INDEX_FREE_TEXTS];

        final String[] freeTexts = Split.byColon(freeTextComponent);
        if (freeTexts.length > MAXIMUM_FREE_TEXTS) {
            throw new IllegalArgumentException("Can't create "
                + ReferencePopulationDefinitionFreeText.class.getSimpleName() + " from " + edifact);
        }

        final String[] filteredFreeTexts = Arrays.stream(freeTexts)
            .filter(line -> !StringUtils.isBlank(line))
            .toArray(String[]::new);
        return new ReferencePopulationDefinitionFreeText(filteredFreeTexts);
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
            + String.join(COLON_SEPARATOR, freeTexts);
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // no stateful fields to validate
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (freeTexts.length == 0 || freeTexts[0].isBlank()) {
            throw new EdifactValidationException(KEY_QUALIFIER
                + ": At least one reference population definition must be given.");
        }

        if (freeTexts.length > MAXIMUM_FREE_TEXTS) {
            throw new EdifactValidationException(KEY_QUALIFIER
                + ": At most " + MAXIMUM_FREE_TEXTS + " reference population definitions may be given.");
        }
    }
}
