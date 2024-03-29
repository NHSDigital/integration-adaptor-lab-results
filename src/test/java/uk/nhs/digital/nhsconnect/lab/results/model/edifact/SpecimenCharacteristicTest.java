package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenCharacteristicTest {

    @Test
    void testGetKey() {
        final var segment = new SpecimenCharacteristic(null, null);

        assertThat(segment.getKey()).isEqualTo("SPC");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> SpecimenCharacteristic.fromString("WRONG"))
            .withMessage("Can't create SpecimenCharacteristic from WRONG");
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
    void testValidateNoneGiven() {
        final var segment = SpecimenCharacteristic.fromString("SPC+TSP+");

        assertThatThrownBy(segment::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: at least one of characteristic and typeOfSpecimen is required");
    }
}
