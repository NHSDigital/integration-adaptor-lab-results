package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A specialisation of a segment for the specific use case of an interchange header
 * takes in specific values required to generate an interchange header
 * example: UNB+UNOA:2+TES5+XX11+920113:1317+00000002'
 */
@Getter
@Builder
public class InterchangeHeader extends Segment {

    protected static final String KEY = "UNB";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd:HHmm")
        .withZone(TimestampService.UK_ZONE);
    public static final long MAX_INTERCHANGE_SEQUENCE = 99_999_999L;

    private static final int SENDER_INDEX = 2;
    private static final int RECIPIENT_INDEX = 3;
    private static final int TRANSLATION_TIME_INDEX = 4;
    private static final int SEQUENCE_NUMBER_INDEX = 5;

    private final String sender;
    private final String recipient;
    private final Long sequenceNumber;
    private final Instant translationTime;

    public static InterchangeHeader fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + InterchangeHeader.class.getSimpleName()
                + " from " + edifactString);
        }

        final var split = Split.byPlus(edifactString);

        return InterchangeHeader.builder()
            .sender(SENDER_INDEX < split.length ? split[SENDER_INDEX] : null)
            .recipient(RECIPIENT_INDEX < split.length ? split[RECIPIENT_INDEX] : null)
            .sequenceNumber(SEQUENCE_NUMBER_INDEX < split.length ? parseLong(split[SEQUENCE_NUMBER_INDEX]) : null)
            .translationTime(TRANSLATION_TIME_INDEX < split.length
                ? getTranslationTime(split[TRANSLATION_TIME_INDEX])
                : null)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(getSender())) {
            throw new EdifactValidationException(getKey() + ": Attribute sender is required");
        }
        if (StringUtils.isBlank(getRecipient())) {
            throw new EdifactValidationException(getKey() + ": Attribute recipient is required");
        }
        if (getTranslationTime() == null) {
            throw new EdifactValidationException(getKey() + ": Attribute translationTime is required");
        }
        if (getSequenceNumber() == null) {
            throw new EdifactValidationException(getKey() + ": Attribute sequenceNumber is required");
        }
        if (getSequenceNumber() < 1 || getSequenceNumber() > MAX_INTERCHANGE_SEQUENCE) {
            throw new EdifactValidationException(
                getKey() + ": Attribute sequenceNumber must be between 1 and " + MAX_INTERCHANGE_SEQUENCE);
        }
    }

    private static Long parseLong(String longString) {
        if (StringUtils.isBlank(longString)) {
            return null;
        }
        try {
            return Long.parseLong(longString);
        } catch (Exception ex) {
            throw new EdifactValidationException(KEY + ": Error parsing long value", ex);
        }
    }

    public static Instant getTranslationTime(String translationTimeStr) {
        if (StringUtils.isBlank(translationTimeStr)) {
            return null;
        }
        ZonedDateTime translationTime;
        try {
            translationTime = ZonedDateTime.parse(
                translationTimeStr,
                DateTimeFormatter.ofPattern("yyMMdd:HHmm").withZone(TimestampService.UK_ZONE));
        } catch (Exception ex) {
            throw new EdifactValidationException(KEY + ": Error parsing time", ex);
        }
        return translationTime.toInstant();
    }
}
