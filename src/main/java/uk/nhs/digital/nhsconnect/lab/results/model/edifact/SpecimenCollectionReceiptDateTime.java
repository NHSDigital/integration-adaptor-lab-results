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

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Example DTM+SRI:201002241541:203'
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Builder
public class SpecimenCollectionReceiptDateTime extends Segment {
    protected static final String KEY = "DTM";
    private static final String QUALIFIER = "SRI";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @NonNull
    private final String collectionReceiptDateTime;
    @NonNull
    private final DateFormat dateFormat;

    public static SpecimenCollectionReceiptDateTime fromString(final String edifactString) {
        if (!edifactString.startsWith(SpecimenCollectionReceiptDateTime.KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenCollectionReceiptDateTime.class.getSimpleName() + " from " + edifactString);
        }
        final String input = Split.byPlus(edifactString)[1];
        final String collectionReceiptDateTime = Split.byColon(input)[1];
        final String format = Split.bySegmentTerminator(Split.byColon(input)[2])[0];
        final SpecimenCollectionReceiptDateTime.SpecimenCollectionReceiptDateTimeBuilder
            collectionReceiptDateTimeBuilder = SpecimenCollectionReceiptDateTime.builder();

        if (isBlank(collectionReceiptDateTime) || isBlank(format)) {
            throw new IllegalArgumentException("Can't create SpecimenCollectionReceiptDateTime from " + edifactString
                + " because of missing date-time and/or format definition");
        }
        final String formattedFhirDate = getFormattedFhirDate(collectionReceiptDateTime, format);
        collectionReceiptDateTimeBuilder
            .collectionReceiptDateTime(formattedFhirDate)
            .dateFormat(DateFormat.fromCode(format));

        return collectionReceiptDateTimeBuilder.build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            .concat(COLON_SEPARATOR)
            .concat(getFormattedEdifactDate(collectionReceiptDateTime, dateFormat))
            .concat(COLON_SEPARATOR)
            .concat(dateFormat.getCode());
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (collectionReceiptDateTime.isBlank()) {
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
        throw new IllegalArgumentException(KEY + ": Date format code " + dateFormat + " is not supported");
    }

    private static String getFormattedEdifactDate(final String collectionDateTime, final DateFormat dateFormat) {
        if (dateFormat.equals(DateFormat.CCYYMMDD)) {
            return DATE_FORMATTER_CCYYMMDD.format(LocalDate.parse(collectionDateTime));
        } else if (dateFormat.equals(DateFormat.CCYYMMDDHHMM)) {
            return DATE_FORMATTER_CCYYMMDDHHMM.format(OffsetDateTime.parse(collectionDateTime));
        }
        throw new IllegalArgumentException(KEY + ": Date format " + dateFormat.name() + " is not supported");
    }
}
