package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Example 1: DTM+SCO:20100223:102'
 * Example 2: DTM+SCO:201002231541:203'
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Builder
public class SpecimenCollectionDateTime extends Segment {

    protected final static String KEY = "DTM";
    private final static String QUALIFIER = "SCO";
    private final static String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @NonNull
    private final String collectionDateTime;
    @NonNull
    private final DateFormat dateFormat;

    public static SpecimenCollectionDateTime fromString(final String edifactString) {
        if (!edifactString.startsWith(SpecimenCollectionDateTime.KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + SpecimenCollectionDateTime.class.getSimpleName() + " from " + edifactString);
        }
        final String input = Split.byPlus(edifactString)[1];
        final String collectionDateTime = Split.byColon(input)[1];
        final String format = Split.bySegmentTerminator(Split.byColon(input)[2])[0];
        final SpecimenCollectionDateTimeBuilder collectionDateTimeBuilder = SpecimenCollectionDateTime.builder();

        if (isNotBlank(collectionDateTime) && isNotBlank(format)) {
            final String formattedFhirDate = getFormattedFhirDate(collectionDateTime, format);
            collectionDateTimeBuilder
                .collectionDateTime(formattedFhirDate)
                .dateFormat(DateFormat.fromCode(format));
        }

        return collectionDateTimeBuilder.build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            .concat(COLON_SEPARATOR)
            .concat(getFormattedEdifactDate(collectionDateTime, dateFormat))
            .concat(COLON_SEPARATOR)
            .concat(dateFormat.getCode());
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (collectionDateTime.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Date/time of sample collection is required");
        }
    }

    private static String getFormattedFhirDate(final String collectionDateTime, final String dateFormat) {
        if (dateFormat.equals(DateFormat.CCYYMMDD.getCode())) {
            return LocalDate.parse(collectionDateTime, DATE_FORMATTER_CCYYMMDD).toString();
        } else if (dateFormat.equals(DateFormat.CCYYMMDDHHMM.getCode())) {
            LocalDateTime localDateTime = LocalDateTime.parse(collectionDateTime, DATE_FORMATTER_CCYYMMDDHHMM);
            return localDateTime.toString() + "+00:00";
        }
        throw new UnsupportedOperationException(KEY + ": Date format code " + dateFormat + " is not supported");
    }

    private static String getFormattedEdifactDate(final String collectionDateTime, final DateFormat dateFormat) {
        if (dateFormat.equals(DateFormat.CCYYMMDD)) {
            return DATE_FORMATTER_CCYYMMDD.format(LocalDate.parse(collectionDateTime));
        } else if (dateFormat.equals(DateFormat.CCYYMMDDHHMM)) {
            return DATE_FORMATTER_CCYYMMDDHHMM.format(OffsetDateTime.parse(collectionDateTime));
        }
        throw new UnsupportedOperationException(KEY + ": Date format " + dateFormat.name() + " is not supported");
    }
}
