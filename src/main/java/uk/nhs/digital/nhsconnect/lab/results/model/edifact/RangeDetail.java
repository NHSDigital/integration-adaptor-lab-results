package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Examples:
 * <br/>
 * {@code RND+U+170+1100'}: between 170 and 1100<br/>
 * {@code RND+U++999'}: less than 999<br/>
 * {@code RND+U+13.3+16.7+g/dl'}: with units
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class RangeDetail extends Segment {
    private static final String KEY = "RND";
    private static final String QUALIFIER = "U";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private static final int INDEX_LOWER_LIMIT = 2;
    private static final int INDEX_UPPER_LIMIT = 3;
    private static final int INDEX_UNITS = 4;

    private final BigDecimal lowerLimit;
    private final BigDecimal upperLimit;

    private final String units;

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
        final String units = split.length > INDEX_UNITS ? split[INDEX_UNITS] : null;

        return new RangeDetail(lowerLimit, upperLimit, units);
    }

    public Optional<BigDecimal> getLowerLimit() {
        return Optional.ofNullable(lowerLimit);
    }

    public Optional<BigDecimal> getUpperLimit() {
        return Optional.ofNullable(upperLimit);
    }

    public Optional<String> getUnits() {
        return Optional.ofNullable(units);
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
