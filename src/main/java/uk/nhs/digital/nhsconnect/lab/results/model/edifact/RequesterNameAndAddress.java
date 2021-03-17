package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.HealthcareRegistrationIdentificationCode;

/**
 * Example: Details of requesting practitioner with identifier
 * <pre>
 * NAD+PO+G3380314:900++SCOTT'
 * SPR+PRO'
 * </pre>
 * Example: Details of requesting practitioner without identifier
 * <pre>
 * NAD+PO+++SCOTT'
 * SPR+PRO'
 * </pre>
 * Example: Details of requesting organization
 * <pre>
 * NAD+PO+++NORTH DOWN GP'
 * SPR+ORG'
 * </pre>
 */
@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class RequesterNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "PO";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String name;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static RequesterNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + RequesterNameAndAddress.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String[] colonSplit = Split.byColon(keySplit[2]);

        String identifier = null;
        HealthcareRegistrationIdentificationCode code = null;
        if (colonSplit.length > 1 && StringUtils.isNotBlank(colonSplit[0])) {
            identifier = colonSplit[0];
            code = StringUtils.isNotBlank(colonSplit[1])
                ? HealthcareRegistrationIdentificationCode.fromCode(colonSplit[1]) : null;
        }

        final String name = keySplit[4];

        return RequesterNameAndAddress.builder()
            .identifier(identifier)
            .code(code)
            .name(name)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(name)) {
            throw new EdifactValidationException(KEY + ": Attribute name is required");
        }
    }
}
