package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Example 1: DTM+SCO:20100223:102'
 * <br>
 * Example 2: DTM+SCO:201002231541:203'
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Builder
public class SpecimenCollectionDateTime extends Segment {
    protected static final String KEY = "DTM";
    private static final String QUALIFIER = "SCO";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final Set<DateFormat> VALID_DATE_FORMATS = Set.of(DateFormat.CCYYMMDD, DateFormat.CCYYMMDDHHMM);

    @NonNull
    private final String collectionDateTime;
    @NonNull
    private final DateFormat dateFormat;

    public static SpecimenCollectionDateTime fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                    "Can't create " + SpecimenCollectionDateTime.class.getSimpleName() + " from " + edifactString);
        }
        final String input = Split.byPlus(edifactString)[1];
        final String collectionDateTime = Split.byColon(input)[1];
        final String format = Split.bySegmentTerminator(Split.byColon(input)[2])[0];

        if (isBlank(collectionDateTime) || isBlank(format)) {
            throw new IllegalArgumentException("Can't create " + SpecimenCollectionDateTime.class.getSimpleName()
                    + " from " + edifactString + " because of missing date-time and/or format definition");
        }
        return SpecimenCollectionDateTime.builder()
                .collectionDateTime(collectionDateTime)
                .dateFormat(DateFormat.fromCode(format))
                .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (collectionDateTime.isBlank()) {
            throw new EdifactValidationException(KEY + ": Date/time of sample collection is required");
        }
        if (!VALID_DATE_FORMATS.contains(dateFormat)) {
            throw new EdifactValidationException(KEY + ": Date format " + dateFormat.getCode() + " unsupported");
        }
    }
}
