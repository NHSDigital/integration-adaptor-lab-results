package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public abstract class FreeTextSegment extends Segment {
    protected static final String KEY = "FTX";
    protected static final int FREE_TEXT_INDEX = 4;
    private static final int MAXIMUM_FREE_TEXTS = 5;

    @NonNull
    private final String qualifier;
    @NonNull
    private final String[] texts;

    protected static String[] extractFreeTextsFromString(final String edifact, final String keyQualifier,
                                                         final String className) {
        if (!edifact.startsWith(keyQualifier)) {
            throw new IllegalArgumentException(String.format("Can't create %s (%s) from %s",
                className, keyQualifier, edifact));
        }

        final String[] split = Split.byPlus(edifact);
        final String freeTextComponent = split[FREE_TEXT_INDEX];

        final String[] freeTexts = Split.byColon(freeTextComponent);
        if (freeTexts.length > MAXIMUM_FREE_TEXTS) {
            throw new IllegalArgumentException(String.format("Can't create %s (%s) from %s because too many free texts",
                className, keyQualifier, edifact));
        }

        return Arrays.stream(freeTexts)
            .filter(line -> !StringUtils.isBlank(line))
            .toArray(String[]::new);
    }

    @Override
    public final String getKey() {
        return KEY;
    }

    @Override
    public final String getValue() {
        return qualifier
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + String.join(COLON_SEPARATOR, texts);
    }

    @Override
    protected final void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public final void preValidate() throws EdifactValidationException {
        if (texts.length == 0 || texts[0].isBlank()) {
            throw new EdifactValidationException(KEY + PLUS_SEPARATOR + qualifier
                + ": At least one free text must be given.");
        }

        if (texts.length > MAXIMUM_FREE_TEXTS) {
            throw new EdifactValidationException(KEY + PLUS_SEPARATOR + qualifier
                + ": At most " + MAXIMUM_FREE_TEXTS + " free texts may be given.");
        }
    }
}
