package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthAuthorityNameAndAddressTest {
    private final HealthAuthorityNameAndAddress healthAuthorityNameAndAddress
        = new HealthAuthorityNameAndAddress("ABC", "code1");

    @Test
    void testGetKey() {
        assertThat(healthAuthorityNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testGetValue() {
        assertThat(healthAuthorityNameAndAddress.getValue()).isEqualTo("FHS+ABC:code1");
    }

    @Test
    void testValidateStateful() {
        assertThatCode(healthAuthorityNameAndAddress::validateStateful).doesNotThrowAnyException();
    }

    @Test
    void testPreValidateEmptyIdentifier() {
        final var emptyIdentifier = new HealthAuthorityNameAndAddress("", "x");
        assertThatThrownBy(emptyIdentifier::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute identifier is required");
    }

    @Test
    void testPreValidateEmptyCode() {
        final var emptyCode = new HealthAuthorityNameAndAddress("x", "");
        assertThatThrownBy(emptyCode::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testFromString() {
        assertThat(HealthAuthorityNameAndAddress.fromString("NAD+FHS+ABC:code1").getValue())
            .isEqualTo(healthAuthorityNameAndAddress.getValue());
    }

    @Test
    void testFromStringInvalidValue() {
        assertThatThrownBy(() -> HealthAuthorityNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
