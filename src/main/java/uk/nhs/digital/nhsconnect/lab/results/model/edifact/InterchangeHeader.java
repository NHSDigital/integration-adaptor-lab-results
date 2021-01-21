package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class InterchangeHeader extends Segment {

    public static final String KEY = "UNB";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd:HHmm").withZone(TimestampService.UK_ZONE);
    public static final long MAX_INTERCHANGE_SEQUENCE = 99_999_999L;
    private static final String RECEP_ENDING = "+RECEP+++EDIFACT TRANSFER";

    private static final int SENDER_INDEX = 2;
    private static final int RECIPIENT_INDEX = 3;
    private static final int TRANSLATION_TIME_INDEX = 4;
    private static final int SEQUENCE_NUMBER_INDEX = 5;

    private @NonNull String sender;
    private @NonNull String recipient;
    private @NonNull Instant translationTime;
    private Long sequenceNumber;

    public static InterchangeHeader fromString(String edifactString) {
        if (!edifactString.startsWith(InterchangeHeader.KEY)) {
            throw new IllegalArgumentException("Can't create " + InterchangeHeader.class.getSimpleName() + " from " + edifactString);
        }

        String[] split = Split.byPlus(edifactString);
        final String sender = split[SENDER_INDEX];
        final String recipient = split[RECIPIENT_INDEX];
        final String translationTimeStr = split[TRANSLATION_TIME_INDEX];
        final Long sequenceNumber = Long.valueOf(split[SEQUENCE_NUMBER_INDEX]);

        ZonedDateTime translationTime = ZonedDateTime.parse(translationTimeStr, DateTimeFormatter.ofPattern("yyMMdd:HHmm").withZone(TimestampService.UK_ZONE));

        return new InterchangeHeader(sender, recipient, translationTime.toInstant(), sequenceNumber);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        String timestamp = DATE_FORMAT.format(translationTime);
        String formattedSequenceNumber = String.format("%08d", sequenceNumber);
        return "UNOA:2" + "+" + sender + "+" + recipient + "+" + timestamp + "+" + formattedSequenceNumber;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        if (sequenceNumber == null) {
            throw new EdifactValidationException(getKey() + ": Attribute sequenceNumber is required");
        }
        if (sequenceNumber < 1 || sequenceNumber > MAX_INTERCHANGE_SEQUENCE) {
            throw new EdifactValidationException(
                getKey() + ": Attribute sequenceNumber must be between 1 and " + MAX_INTERCHANGE_SEQUENCE);
        }
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (sender.isEmpty()) {
            throw new EdifactValidationException(getKey() + ": Attribute sender is required");
        }
        if (recipient.isEmpty()) {
            throw new EdifactValidationException(getKey() + ": Attribute recipient is required");
        }
    }
}
