package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.HealthcareRegistrationIdentificationCode;

/**
 * Example: Details of performing practitioner
 * <pre>
 * NAD+SLA+A2442389:902++DR J SMITH'
 * SPR+PRO'
 * </pre>
 * Example: Details of performing organization's department
 * <pre>
 * NAD+SLA+++Haematology'
 * SPR+DPT'
 * </pre>
 * Example: Details of performing organization with name only
 * <pre>
 * NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL'
 * SPR+ORG'
 * </pre>
 * Example: NHS002 variant of a performing orgnization without a name, only a code
 * <pre>
 * NAD+SLA+REF00:903'
 * SPR+ORG'
 * </pre>
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PerformerNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "SLA";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String name;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static PerformerNameAndAddress fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + PerformerNameAndAddress.class.getSimpleName() + " from " + edifactString
            );
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String[] colonSplit = Split.byColon(keySplit[2]);

        final String identifier = arrayGetSafe(colonSplit, 0).orElse(null);
        final HealthcareRegistrationIdentificationCode code = arrayGetSafe(colonSplit, 1)
            .map(HealthcareRegistrationIdentificationCode::fromCode)
            .orElse(null);
        final String name = arrayGetSafe(keySplit, 4).orElse(null);

        return PerformerNameAndAddress.builder()
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
        if (StringUtils.isBlank(identifier) && StringUtils.isBlank(name)) {
            throw new EdifactValidationException(KEY + ": Attribute identifier or name is required");
        }
    }
}
