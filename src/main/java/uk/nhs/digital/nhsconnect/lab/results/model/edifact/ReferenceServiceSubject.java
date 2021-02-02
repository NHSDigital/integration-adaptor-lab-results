package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * E.g. {@code RFF+SSI:X88442211'}
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceServiceSubject extends Segment {
    private static final String KEY = "RFF";
    private static final String QUALIFIER = "SSI";
    private static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    @NonNull
    private String number;

    public static ReferenceServiceSubject fromString(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + ReferenceServiceSubject.class.getSimpleName()
                + " from " + edifact);
        }

        final String[] split = Split.byColon(edifact);
        return new ReferenceServiceSubject(split[1]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + ":" + number;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // no op
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (StringUtils.isEmpty(number)) {
            throw new EdifactValidationException(KEY + ": Attribute number is required");
        }
    }
}
