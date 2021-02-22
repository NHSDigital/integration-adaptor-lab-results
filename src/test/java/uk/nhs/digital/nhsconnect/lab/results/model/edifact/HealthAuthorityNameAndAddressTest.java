package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthAuthorityNameAndAddressTest {
    private final HealthAuthorityNameAndAddress healthAuthorityNameAndAddress
        = new HealthAuthorityNameAndAddress("ABC", "code1");

    @Test
    void testGetKey() {
        assertThat(healthAuthorityNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testValidateEmptyIdentifier() {
        final var emptyIdentifier = new HealthAuthorityNameAndAddress("", "x");
        assertThatThrownBy(emptyIdentifier::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute identifier is required");
    }

    @Test
    void testValidateEmptyCode() {
        final var emptyCode = new HealthAuthorityNameAndAddress("x", "");
        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testFromString() {
        var fromString = HealthAuthorityNameAndAddress.fromString("NAD+FHS+ABC:code1");

        assertThat(fromString.getCode()).isEqualTo(healthAuthorityNameAndAddress.getCode());
        assertThat(fromString.getIdentifier()).isEqualTo(healthAuthorityNameAndAddress.getIdentifier());
    }

    @Test
    void testFromStringInvalidValue() {
        assertThatThrownBy(() -> HealthAuthorityNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
