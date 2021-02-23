package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * A specialisation of a segment for the specific use case of an interchange trailer
 * takes in specific values required to generate an interchange trailer
 * example: UNZ+1+00000002'.
 */
@Builder
@Getter
public class InterchangeTrailer extends Segment {

    public static final String KEY = "UNZ";

    private static final int NUMBER_OF_MESSAGES_INDEX = 1;
    private static final int SEQUENCE_NUMBER_INDEX = 2;

    private final Long sequenceNumber;
    private final Integer numberOfMessages;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (getNumberOfMessages() < 1) {
            throw new EdifactValidationException(KEY + ": Attribute numberOfMessages is required");
        }
        if (getSequenceNumber() == null || getSequenceNumber() <= 0) {
            throw new EdifactValidationException(KEY + ": Attribute sequenceNumber is required");
        }
    }

    public static InterchangeTrailer fromString(String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + InterchangeTrailer.class.getSimpleName()
                + " from " + edifactString);
        }

        final var split = Split.byPlus(edifactString);

        return InterchangeTrailer.builder()
            .sequenceNumber(
                split.length > SEQUENCE_NUMBER_INDEX ? parseLong(split[SEQUENCE_NUMBER_INDEX]) : null)
            .numberOfMessages(
                split.length > NUMBER_OF_MESSAGES_INDEX ? parseInt(split[NUMBER_OF_MESSAGES_INDEX]) : null)
            .build();
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

    private static Integer parseInt(String intString) {
        if (StringUtils.isBlank(intString)) {
            return null;
        }
        try {
            return Integer.parseInt(intString);
        } catch (Exception ex) {
            throw new EdifactValidationException(KEY + ": Error parsing int value", ex);
        }
    }
}
