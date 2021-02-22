package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.math.BigDecimal;

/**
 * Examples:
 *
 * {@code RND+U+170+1100'}: between 170 and 1100<br/>
 * {@code RND+U++999'}: less than 999<br/>
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@AllArgsConstructor
public class RangeDetail extends Segment {
    private static final String KEY = "RND";
    private static final String QUALIFIER = "U";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private static final int INDEX_LOWER_LIMIT = 2;
    private static final int INDEX_UPPER_LIMIT = 3;

    private final BigDecimal lowerLimit;
    private final BigDecimal upperLimit;

    public static RangeDetail fromString(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + RangeDetail.class.getSimpleName()
                + " from " + edifact);
        }

        final String[] split = Split.byPlus(edifact);
        final BigDecimal lowerLimit = split[INDEX_LOWER_LIMIT].isBlank()
            ? null
            : new BigDecimal(split[INDEX_LOWER_LIMIT]);
        final BigDecimal upperLimit = split[INDEX_UPPER_LIMIT].isBlank()
            ? null
            : new BigDecimal(split[INDEX_UPPER_LIMIT]);

        return new RangeDetail(lowerLimit, upperLimit);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (lowerLimit == null && upperLimit == null) {
            throw new EdifactValidationException(KEY
                + ": At least one of lower reference limit and upper reference limit is required");
        }
    }
}
