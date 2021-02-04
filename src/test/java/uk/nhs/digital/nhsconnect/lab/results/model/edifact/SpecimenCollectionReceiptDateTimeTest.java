package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpecimenCollectionReceiptDateTimeTest {

    private static final String VALID_FHIR_SCDT_CCYYMMDD = "2010-02-23";
    private static final String VALID_FHIR_SCDT_CCYYMMDDHHMM = "2010-02-23T15:41+00:00";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+SRI:20100223:102'";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM = "DTM+SRI:201002231541:203'";
    private static final String VALID_EDIFACT_CCYYMMDD_VALUE = "SRI:20100223:102";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM_VALUE = "SRI:201002231541:203";

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDD() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(VALID_FHIR_SCDT_CCYYMMDD)
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMMDD, actual);
    }

    @Test
    void testToEdifactForValidSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(VALID_FHIR_SCDT_CCYYMMDDHHMM)
            .dateFormat(DateFormat.CCYYMMDDHHMM)
            .build();

        final String actual = specimenCollectionReceiptDateTime.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMMDDHHMM, actual);
    }

    @Test
    void testToEdifactForEmptySpecimenCollectionReceiptDateTimeThrowsException() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime("")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            specimenCollectionReceiptDateTime::toEdifact);

        assertEquals("DTM: Date/time of sample collection is required", exception.getMessage());
    }

    @Test
    void testToEdifactForBlankSpecimenCollectionReceiptDateTimeThrowsException() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime = SpecimenCollectionReceiptDateTime.builder()
            .collectionReceiptDateTime(" ")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            specimenCollectionReceiptDateTime::toEdifact);

        assertEquals("DTM: Date/time of sample collection is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionReceiptDateTimeInFormatCCYYMMDD() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime =
            SpecimenCollectionReceiptDateTime.fromString(VALID_EDIFACT_CCYYMMDD);

        assertAll("fromString specimen collection date format CCYYMMDD",
            () -> assertEquals(SpecimenCollectionDateTime.KEY, specimenCollectionReceiptDateTime.getKey()),
            () -> assertEquals(VALID_EDIFACT_CCYYMMDD_VALUE, specimenCollectionReceiptDateTime.getValue()),
            () -> assertEquals(VALID_FHIR_SCDT_CCYYMMDD, specimenCollectionReceiptDateTime.getCollectionReceiptDateTime()));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionReceiptDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionReceiptDateTime specimenCollectionReceiptDateTime =
            SpecimenCollectionReceiptDateTime.fromString(VALID_EDIFACT_CCYYMMDDHHMM);

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
            () -> assertEquals(SpecimenCollectionDateTime.KEY, specimenCollectionReceiptDateTime.getKey()),
            () -> assertEquals(VALID_EDIFACT_CCYYMMDDHHMM_VALUE, specimenCollectionReceiptDateTime.getValue()),
            () -> assertEquals(VALID_FHIR_SCDT_CCYYMMDDHHMM, specimenCollectionReceiptDateTime.getCollectionReceiptDateTime()));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SpecimenCollectionReceiptDateTime.fromString("wrong value"));

        assertEquals("Can't create SpecimenCollectionReceiptDateTime from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        final UnsupportedOperationException exception =
            assertThrows(UnsupportedOperationException.class, () -> SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:100'"));

        assertEquals("DTM: Date format code 100 is not supported", exception.getMessage());
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SpecimenCollectionReceiptDateTime.fromString("DTM+SRI:20100223:'"));

        assertEquals("Can't create SpecimenCollectionReceiptDateTime from DTM+SRI:20100223:'"
            + " because of missing date-time and/or format definition", exception.getMessage());
    }
}
