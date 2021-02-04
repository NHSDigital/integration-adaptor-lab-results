package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecimenCollectionDateTimeTest {

    private static final String VALID_FHIR_SCDT_CCYYMMDD = "2010-02-23";
    private static final String VALID_FHIR_SCDT_CCYYMMDDHHMM = "2010-02-23T15:41+00:00";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+SCO:20100223:102'";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM = "DTM+SCO:201002231541:203'";
    private static final String VALID_EDIFACT_CCYYMMDD_VALUE = "SCO:20100223:102";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM_VALUE = "SCO:201002231541:203";

    @Test
    void testToEdifactForValidSpecimenCollectionDateTimeInFormatCCYYMMDD() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
            .collectionDateTime(VALID_FHIR_SCDT_CCYYMMDD)
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final String actual = specimenCollectionDateTime.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMMDD, actual);
    }

    @Test
    void testToEdifactForValidSpecimenCollectionDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
            .collectionDateTime(VALID_FHIR_SCDT_CCYYMMDDHHMM)
            .dateFormat(DateFormat.CCYYMMDDHHMM)
            .build();

        final String actual = specimenCollectionDateTime.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMMDDHHMM, actual);
    }

    @Test
    void testToEdifactForEmptySpecimenCollectionDateTimeThrowsException() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
            .collectionDateTime("")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            specimenCollectionDateTime::toEdifact);

        assertEquals("DTM: Date/time of sample collection is required", exception.getMessage());
    }

    @Test
    void testToEdifactForBlankSpecimenCollectionDateTimeThrowsException() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.builder()
            .collectionDateTime(" ")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            specimenCollectionDateTime::toEdifact);

        assertEquals("DTM: Date/time of sample collection is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDD() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.fromString(VALID_EDIFACT_CCYYMMDD);

        assertAll("fromString specimen collection date format CCYYMMDD",
            () -> assertEquals(SpecimenCollectionDateTime.KEY, specimenCollectionDateTime.getKey()),
            () -> assertEquals(VALID_EDIFACT_CCYYMMDD_VALUE, specimenCollectionDateTime.getValue()),
            () -> assertEquals(VALID_FHIR_SCDT_CCYYMMDD, specimenCollectionDateTime.getCollectionDateTime()));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDDHHMM() {
        final SpecimenCollectionDateTime specimenCollectionDateTime = SpecimenCollectionDateTime.fromString(VALID_EDIFACT_CCYYMMDDHHMM);

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
            () -> assertEquals(SpecimenCollectionDateTime.KEY, specimenCollectionDateTime.getKey()),
            () -> assertEquals(VALID_EDIFACT_CCYYMMDDHHMM_VALUE, specimenCollectionDateTime.getValue()),
            () -> assertEquals(VALID_FHIR_SCDT_CCYYMMDDHHMM, specimenCollectionDateTime.getCollectionDateTime()));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SpecimenCollectionDateTime.fromString("wrong value"));

        assertEquals("Can't create SpecimenCollectionDateTime from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        final UnsupportedOperationException exception =
            assertThrows(UnsupportedOperationException.class, () -> SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:100'"));

        assertEquals("DTM: Date format code 100 is not supported", exception.getMessage());
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:'"));

        assertEquals("Can't create SpecimenCollectionDateTime from DTM+SCO:20100223:'"
            + " because of missing date-time and/or format definition", exception.getMessage());
    }
}
