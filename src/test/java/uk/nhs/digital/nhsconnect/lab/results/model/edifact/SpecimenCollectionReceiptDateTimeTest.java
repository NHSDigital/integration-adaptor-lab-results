package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCollectionReceiptDateTimeTest {

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDD() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime("20100223")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertThat(actual).isEqualTo("DTM+SRI:20100223:102'");
    }

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime("201002231541")
            .dateFormat(DateFormat.CCYYMMDDHHMM)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertThat(actual).isEqualTo("DTM+SRI:201002231541:203'");
    }

    @Test
    void testToEdifactForEmptySpecimenCollectionReceiptDateTimeThrowsException() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime("")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        assertThatThrownBy(specimenCollectionReceiptDateTime::toEdifact)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("DTM: Date/time of sample collection is required");
    }

    @Test
    void testToEdifactForBlankSpecimenCollectionReceiptDateTimeThrowsException() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(" ")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        assertThatThrownBy(specimenCollectionReceiptDateTime::toEdifact)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("DTM: Date/time of sample collection is required");
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionReceiptDateTimeInFormatCCYYMMDD() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime =
            SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:102'");

        assertAll("fromString specimen collection date format CCYYMMDD",
            () -> assertThat(specimenCollectionReceiptDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionReceiptDateTime.getCollectionReceiptDateTime())
                .isEqualTo("20100223"));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime =
            SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:201002231541:203'");

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
            () -> assertThat(specimenCollectionReceiptDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionReceiptDateTime.getCollectionReceiptDateTime())
                .isEqualTo("201002231541"));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionReceiptDateTime.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SpecimenCollectionReceiptDateTime from wrong value");
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:100'"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No dateFormat name for '100'");
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:'"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SpecimenCollectionReceiptDateTime from DTM+SRI:20100223:'"
                + " because of missing date-time and/or format definition");
    }
}
