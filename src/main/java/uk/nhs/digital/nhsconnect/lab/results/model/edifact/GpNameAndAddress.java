package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example NAD+GP+2750922,295:900'
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class GpNameAndAddress extends Segment {
    public static final String KEY = "NAD";
    public static final String QUALIFIER = "GP";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    @NonNull
    private String identifier;

    @NonNull
    private String code;

    public static GpNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + GpNameAndAddress.class.getSimpleName()
                    + " from " + edifactString);
        }
        final String[] keySplit = Split.byPlus(edifactString);
        final String identifier = Split.byColon(keySplit[2])[0];
        final String code = Split.byColon(keySplit[2])[1];
        return new GpNameAndAddress(identifier, code);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(identifier)) {
            throw new EdifactValidationException(KEY + ": Attribute identifier is required");
        }
        if (StringUtils.isBlank(code)) {
            throw new EdifactValidationException(KEY + ": Attribute code is required");
        }
    }
}
