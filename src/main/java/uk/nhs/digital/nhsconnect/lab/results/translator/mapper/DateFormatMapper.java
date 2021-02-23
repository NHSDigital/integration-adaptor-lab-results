package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import org.hl7.fhir.dstu3.model.BaseDateTimeType;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DateType;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.regex.Pattern;

@Component
public class DateFormatMapper {
    private static final Pattern DATE_PATTERN = Pattern.compile("^(?<yy>\\d{4})"
        + "(?<MM>\\d{2})?"
        + "(?<dd>\\d{2})?"
        + "(?<hh>\\d{2})?"
        + "(?<mm>\\d{2})?$"
    );

    public BaseDateTimeType mapToDateTimeType(final DateFormat format, final String value) {
        final var date = mapToDate(format, value);
        switch (format) {
            case CCYY:
                return new DateType(date, TemporalPrecisionEnum.YEAR);
            case CCYYMM:
                return new DateType(date, TemporalPrecisionEnum.MONTH);
            case CCYYMMDD:
                return new DateType(date, TemporalPrecisionEnum.DAY);
            case CCYYMMDDHHMM:
                // MINUTE precision is not supported
                return new DateTimeType(date, TemporalPrecisionEnum.SECOND);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format.getCode());
        }
    }

    public Date mapToDate(final DateFormat format, final String value) {
        final var matcher = DATE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot interpret " + value + " as " + format.name());
        }
        // all formats have the year
        final String yy = matcher.group("yy");
        final int yearValue = Integer.parseInt(yy);

        switch (format) {
            case CCYY:
                throwIfPresent(matcher.group("MM"), "Cannot interpret " + value + " as CCYY");
                return Date.from(LocalDate.of(yearValue, 1, 1)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC));
            case CCYYMM:
                throwIfPresent(matcher.group("dd"), "Cannot interpret " + value + " as CCYYMM");
                return Date.from(LocalDate.of(
                    yearValue,
                    Integer.parseInt(matcher.group("MM")),
                    1
                ).atStartOfDay()
                    .toInstant(ZoneOffset.UTC));
            case CCYYMMDD:
                throwIfPresent(matcher.group("hh"), "Cannot interpret " + value + " as CCYYMMDD");
                return Date.from(LocalDate.of(
                    yearValue,
                    Integer.parseInt(matcher.group("MM")),
                    Integer.parseInt(matcher.group("dd"))
                ).atStartOfDay()
                    .toInstant(ZoneOffset.UTC));
            case CCYYMMDDHHMM:
                return Date.from(LocalDateTime.of(
                    yearValue,
                    Integer.parseInt(matcher.group("MM")),
                    Integer.parseInt(matcher.group("dd")),
                    Integer.parseInt(matcher.group("hh")),
                    Integer.parseInt(matcher.group("mm"))
                ).toInstant(ZoneOffset.UTC));
            default:
                throw new IllegalArgumentException("Unsupported format: " + format.getCode());
        }
    }

    private static void throwIfPresent(final Object value, final String message) {
        if (value != null) {
            throw new IllegalArgumentException(message);
        }
    }
}
