package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Arrays;

/**
 * <pre>
 * Example: {@code FTX+CID+++ON WARFARIN'}
 * </pre>
 * <pre>
 * Example: {@code FTX+SPC+++Normal. Sent following an initial single Negative
 * result or following:2 Negative results after an initial Weak Positive'}
 * </pre>
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class FreeTextSegment extends Segment {
    public static final String KEY = "FTX";
    private static final int MAXIMUM_FREE_TEXTS = 5;

    @NonNull
    private final FreeTextType type;
    @NonNull
    private final String[] texts;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static FreeTextSegment fromString(final String edifact) {
        if (!edifact.startsWith(KEY)) {
            throw new IllegalArgumentException(
                "Can't create " + FreeTextSegment.class.getSimpleName() + " from " + edifact);
        }

        final String[] split = Split.byPlus(edifact);
        final String typeCode = split[1];
        final String freeTextComponent = split[4];

        final String[] freeTexts = Split.byColon(freeTextComponent);
        if (freeTexts.length > MAXIMUM_FREE_TEXTS) {
            throw new IllegalArgumentException(
                "Can't create " + FreeTextSegment.class.getSimpleName() + " from " + edifact
                    + " because too many free texts");
        }

        final var texts = Arrays.stream(freeTexts)
            .filter(line -> !StringUtils.isBlank(line))
            .toArray(String[]::new);
        return new FreeTextSegment(FreeTextType.fromCode(typeCode), texts);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (texts.length == 0 || texts[0].isBlank()) {
            throw new EdifactValidationException(KEY + PLUS_SEPARATOR + type.getQualifier()
                + ": At least one free text must be given.");
        }

        if (texts.length > MAXIMUM_FREE_TEXTS) {
            throw new EdifactValidationException(KEY + PLUS_SEPARATOR + type.getQualifier()
                + ": At most " + MAXIMUM_FREE_TEXTS + " free texts may be given.");
        }
    }
}
