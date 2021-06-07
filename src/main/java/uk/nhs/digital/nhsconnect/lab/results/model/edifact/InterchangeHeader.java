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
import java.util.Arrays;

/**
 * A specialisation of a segment for the specific use case of an interchange header
 * takes in specific values required to generate an interchange header
 * example: UNB+UNOC:3+000000004400001:80+000000024600002:80+100301:1751+1015++MEDRPT++1'
 */
@Getter
@Builder
public class InterchangeHeader extends Segment {

    public enum MessageType {
        MEDRPT, NHSACK
    }

    protected static final String KEY = "UNB";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd:HHmm")
        .withZone(TimestampService.UK_ZONE);
    public static final long MAX_INTERCHANGE_SEQUENCE = 99_999_999L;

    private static final int SENDER_INDEX = 2;
    private static final int RECIPIENT_INDEX = 3;
    private static final int TRANSLATION_TIME_INDEX = 4;
    private static final int SEQUENCE_NUMBER_INDEX = 5;
    private static final int MESSAGE_TYPE_INDEX = 7;
    private static final int NHSACK_INDEX = 9;
    private static final int SENDER_SUBSECTION_ID_INDEX = 0;
    private static final int RECIPIENT_SUBSECTION_ID_INDEX = 0;

    private final String sender;
    private final String recipient;
    private final Long sequenceNumber;
    private final Instant translationTime;
    private final boolean nhsAckRequested;
    private final String messageType;

    public static InterchangeHeader fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + InterchangeHeader.class.getSimpleName()
                + " from " + edifactString);
        }

        final var split = Split.byPlus(edifactString);

        final String sender = SENDER_INDEX < split.length
            ? Split.byColon(split[SENDER_INDEX])[SENDER_SUBSECTION_ID_INDEX] : null;
        final String recipient = RECIPIENT_INDEX < split.length
            ? Split.byColon(split[RECIPIENT_INDEX])[RECIPIENT_SUBSECTION_ID_INDEX] : null;

        return InterchangeHeader.builder()
            .sender(sender)
            .recipient(recipient)
            .sequenceNumber(SEQUENCE_NUMBER_INDEX < split.length ? parseLong(split[SEQUENCE_NUMBER_INDEX]) : null)
            .translationTime(TRANSLATION_TIME_INDEX < split.length
                ? getTranslationTime(split[TRANSLATION_TIME_INDEX])
                : null)
            .nhsAckRequested(NHSACK_INDEX < split.length && split[NHSACK_INDEX].equals("1"))
            .messageType(MESSAGE_TYPE_INDEX < split.length ? split[MESSAGE_TYPE_INDEX] : null)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(getSender())) {
            throw new EdifactValidationException(KEY + ": Attribute sender is required");
        }
        if (StringUtils.isBlank(getRecipient())) {
            throw new EdifactValidationException(KEY + ": Attribute recipient is required");
        }
        if (getTranslationTime() == null) {
            throw new EdifactValidationException(KEY + ": Attribute translationTime is required");
        }
        if (getSequenceNumber() == null) {
            throw new EdifactValidationException(KEY + ": Attribute sequenceNumber is required");
        }
        if (getSequenceNumber() < 1 || getSequenceNumber() > MAX_INTERCHANGE_SEQUENCE) {
            throw new EdifactValidationException(
                getKey() + ": Attribute sequenceNumber must be between 1 and " + MAX_INTERCHANGE_SEQUENCE);
        }
        if (messageType == null || Arrays.stream(MessageType.values()).map(Enum::name).noneMatch(messageType::equals)) {
            throw new EdifactValidationException(
                KEY + ": Attribute messageType must be one of: " + Arrays.toString(MessageType.values()));
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
        try {
            var translationTime = ZonedDateTime.parse(
                translationTimeStr,
                DateTimeFormatter.ofPattern("yyMMdd:HHmm").withZone(TimestampService.UK_ZONE));
            return translationTime.toInstant();
        } catch (Exception ex) {
            throw new EdifactValidationException(KEY + ": Error parsing time", ex);
        }
    }
}
