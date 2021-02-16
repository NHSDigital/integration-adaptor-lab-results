package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCollectionReceiptDateTimeTest {

    private static final String VALID_FHIR_SCDT_CCYYMMDD = "2010-02-23";
    private static final String VALID_FHIR_SCDT_CCYYMMDDHHMM = "2010-02-23T15:41+00:00";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+SRI:20100223:102'";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM = "DTM+SRI:201002231541:203'";
    private static final String VALID_EDIFACT_CCYYMMDD_VALUE = "SRI:20100223:102";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM_VALUE = "SRI:201002231541:203";

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDD() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(VALID_FHIR_SCDT_CCYYMMDD)
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertThat(actual).isEqualTo(VALID_EDIFACT_CCYYMMDD);
    }

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final var specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(VALID_FHIR_SCDT_CCYYMMDDHHMM)
            .dateFormat(DateFormat.CCYYMMDDHHMM)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertThat(actual).isEqualTo(VALID_EDIFACT_CCYYMMDDHHMM);
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
            SpecimenCollectionReceiptDateTime.fromString(VALID_EDIFACT_CCYYMMDD);

        assertAll("fromString specimen collection date format CCYYMMDD",
            () -> assertThat(specimenCollectionReceiptDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionReceiptDateTime.getValue()).isEqualTo(VALID_EDIFACT_CCYYMMDD_VALUE),
            () -> assertThat(specimenCollectionReceiptDateTime.getCollectionReceiptDateTime())
                .isEqualTo(VALID_FHIR_SCDT_CCYYMMDD));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime =
            SpecimenCollectionReceiptDateTime.fromString(VALID_EDIFACT_CCYYMMDDHHMM);

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
            () -> assertThat(specimenCollectionReceiptDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionReceiptDateTime.getValue()).isEqualTo(VALID_EDIFACT_CCYYMMDDHHMM_VALUE),
            () -> assertThat(specimenCollectionReceiptDateTime.getCollectionReceiptDateTime())
                .isEqualTo(VALID_FHIR_SCDT_CCYYMMDDHHMM));
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
            .hasMessage("DTM: Date format code 100 is not supported");
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:'"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SpecimenCollectionReceiptDateTime from DTM+SRI:20100223:'"
                + " because of missing date-time and/or format definition");
    }
}
