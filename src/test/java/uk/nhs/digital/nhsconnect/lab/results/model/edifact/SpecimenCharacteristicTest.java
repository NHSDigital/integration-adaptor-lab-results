package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCharacteristicTest {

    @Test
    void testGetKey() {
        final var segment = new SpecimenCharacteristic(null, null);

        assertThat(segment.getKey()).isEqualTo("SPC");
    }

    @Test
    void testFromStringNoFields() {
        final var segment = SpecimenCharacteristic.fromString("SPC+TSP+");

        assertAll(
            () -> assertThat(segment.getCharacteristic()).isEmpty(),
            () -> assertThat(segment.getTypeOfSpecimen()).isEmpty()
        );
    }

    @Test
    void testFromStringAllFields() {
        final var segment = SpecimenCharacteristic.fromString("SPC+TSP+A012:::Free text");

        assertAll(
            () -> assertThat(segment.getCharacteristic()).contains("A012"),
            () -> assertThat(segment.getTypeOfSpecimen()).contains("Free text")
        );
    }

    @Test
    void testValidateNoneRequired() {
        final var segment = SpecimenCharacteristic.fromString("SPC+TSP+");

        assertThatNoException().isThrownBy(segment::validate);
    }
}
