package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCollectionDateTimeTest {

    private static final String VALID_FHIR_SCDT_CCYYMMDD = "2010-02-23";
    private static final String VALID_FHIR_SCDT_CCYYMMDDHHMM = "2010-02-23T15:41+00:00";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+SCO:20100223:102'";
    private static final String VALID_EDIFACT_CCYYMMDDHHMM = "DTM+SCO:201002231541:203'";

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDD() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.fromString(VALID_EDIFACT_CCYYMMDD);

        assertAll("fromString specimen collection date format CCYYMMDD",
            () -> assertThat(specimenCollectionDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionDateTime.getCollectionDateTime()).isEqualTo(VALID_FHIR_SCDT_CCYYMMDD));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsSpecimenCollectionDateTimeInFormatCCYYMMDDHHMM() {
        final var specimenCollectionDateTime = SpecimenCollectionDateTime.fromString(VALID_EDIFACT_CCYYMMDDHHMM);

        assertAll("fromString specimen collection date format CCYYMMDDHHMM",
            () -> assertThat(specimenCollectionDateTime.getKey()).isEqualTo(SpecimenCollectionDateTime.KEY),
            () -> assertThat(specimenCollectionDateTime.getCollectionDateTime())
                .isEqualTo(VALID_FHIR_SCDT_CCYYMMDDHHMM));
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
            .hasMessage("DTM: Date format code 100 is not supported");
    }

    @Test
    void testFromStringWithBlankDateFormatCodeThrowsException() {
        assertThatThrownBy(() -> SpecimenCollectionDateTime.fromString("DTM+SCO:20100223:'"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SpecimenCollectionDateTime from DTM+SCO:20100223:'"
                + " because of missing date-time and/or format definition");
    }
}
