package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCollectionDateTimeTest {

    @Test
    void testGetCollectionDateTimeForValidSpecimenCollectionDateTimeInFormatCCYYMMDD() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
                .collectionDateTime("20100223")
                .dateFormat(DateFormat.CCYYMMDD)
                .build();

        final String actual = specimenCollectionDateTime.getCollectionDateTime();

        assertThat(actual).isEqualTo("20100223");
    }

    @Test
    void testGetCollectionReceiptDateTimeForValidSpecimenCollectionDateTimeInFormatCCYYMMDDHHMM() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
                .collectionDateTime("201002231541")
                .dateFormat(DateFormat.CCYYMMDDHHMM)
                .build();

        final String actual = specimenCollectionDateTime.getCollectionDateTime();

        assertThat(actual).isEqualTo("201002231541");
    }

    @Test
    void testValidateForEmptySpecimenCollectionDateTimeThrowsException() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
                .collectionDateTime("")
                .dateFormat(DateFormat.CCYYMMDD)
                .build();

        assertThatThrownBy(specimenCollectionDateTime::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("DTM: Date/time of sample collection is required");
    }

    @Test
    void testValidateForBlankSpecimenCollectionDateTimeThrowsException() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
                .collectionDateTime(" ")
                .dateFormat(DateFormat.CCYYMMDD)
                .build();

        assertThatThrownBy(specimenCollectionDateTime::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("DTM: Date/time of sample collection is required");
    }

    @ParameterizedTest
    @CsvSource({"602,2021", "610,202101"})
    void testValidateUnsupportedDateFormatThrowsException(final String format, final String value) {
        final var specimenCollectionDateTime =
                SpecimenCollectionDateTime.fromString("DTM+SCO:" + value + ":" + format);

        assertThatThrownBy(specimenCollectionDateTime::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("DTM: Date format %s unsupported", format);
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDD() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:102'");

        assertAll("fromString specimen collection date format CCYYMMDD",
                () -> assertThat(specimenCollectionDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
                () -> assertThat(specimenCollectionDateTime.getCollectionDateTime()).isEqualTo("20100223"));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDDHHMM() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.fromString("DTM+SCO:201002231541:203'");

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
                () -> assertThat(specimenCollectionDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
                () -> assertThat(specimenCollectionDateTime.getCollectionDateTime())
                        .isEqualTo("201002231541"));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionDateTime.fromString("wrong value"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can't create SpecimenCollectionDateTime from wrong value");
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:100'"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("No dateFormat name for '100'");
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:'"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can't create SpecimenCollectionDateTime from DTM+SCO:20100223:'"
                        + " because of missing date-time and/or format definition");
    }
}
