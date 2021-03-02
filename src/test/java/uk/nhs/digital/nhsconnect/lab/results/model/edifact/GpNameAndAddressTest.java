package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GpNameAndAddressTest {
    private final GpNameAndAddress gpNameAndAddress = new GpNameAndAddress("ABC", "code1");

    @Test
    void testGetKey() {
        assertThat(gpNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testValidateEmptyIdentifier() {
        final GpNameAndAddress emptyIdentifier = new GpNameAndAddress("", "x");
        assertThatThrownBy(emptyIdentifier::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute identifier is required");
    }

    @Test
    void testValidateEmptyCode() {
        final GpNameAndAddress emptyCode = new GpNameAndAddress("x", "");
        assertThatThrownBy(emptyCode::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testFromString() {
        var fromString = GpNameAndAddress.fromString("NAD+GP+ABC:code1");

        assertThat(fromString.getIdentifier()).isEqualTo(gpNameAndAddress.getIdentifier());
        assertThat(fromString.getCode()).isEqualTo(gpNameAndAddress.getCode());
        assertThatThrownBy(() -> GpNameAndAddress.fromString("wrong value"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void when_buildingWithoutMandatoryFields_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> GpNameAndAddress.builder().build());
    }
}
