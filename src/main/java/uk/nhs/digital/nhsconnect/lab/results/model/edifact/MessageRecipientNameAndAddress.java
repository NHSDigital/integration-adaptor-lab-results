package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example NAD+MR+G3380314:900++SCOTT
 */
@Getter
@RequiredArgsConstructor
public class MessageRecipientNameAndAddress extends Segment {
    private static final String KEY = "NAD";
    private static final String QUALIFIER = "MR";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int MESSAGE_RECIPIENT_NAME_INDEX = 4;

    @NonNull
    private final String identifier;

    @NonNull
    private final HealthcareRegistrationIdentificationCode healthcareRegistrationIdentificationCode;

    private final String messageRecipientName;

    public static MessageRecipientNameAndAddress fromString(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                    "Can't create " + MessageRecipientNameAndAddress.class.getSimpleName() + " from " + edifact);
        }

        String[] keySplit = Split.byPlus(edifact);
        String identifier = Split.byColon(keySplit[2])[0];
        String code = Split.byColon(keySplit[2])[1];
        String recipientName = keySplit.length > MESSAGE_RECIPIENT_NAME_INDEX
                ? keySplit[MESSAGE_RECIPIENT_NAME_INDEX]
                : null;

        return new MessageRecipientNameAndAddress(identifier, HealthcareRegistrationIdentificationCode.fromCode(code),
                recipientName);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (identifier.isBlank()) {
            throw new EdifactValidationException(KEY + ": Attribute identifier is required");
        }
    }
}
