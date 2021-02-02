package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class UnstructuredAddressTest {

    @Test
    void testGetKey() {
        assertThat(new UnstructuredAddress(null, null, null).getKey())
            .isEqualTo("ADR");
    }

    @Test
    void testFromStringWrongPrefix() {
        assertThatThrownBy(() -> UnstructuredAddress.fromString("WRONG++BLAH:BLAH"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create UnstructuredAddress from WRONG++BLAH:BLAH");
    }

    @Test
    void testFromStringValidWithAllFields() {
        final var address = UnstructuredAddress.fromString("ADR++US:LINE1:LINE2:LINE3:LINE4:LINE5++POSTCODE");
        assertAll(
            () -> assertThat(address.getFormat()).isEqualTo("US"),
            () -> assertThat(address.getValue()).isEqualTo("US:LINE1:LINE2:LINE3:LINE4:LINE5++POSTCODE"),
            () -> assertThat(address.getAddressLines()).containsExactly("LINE1", "LINE2", "LINE3", "LINE4", "LINE5"),
            () -> assertThat(address.getPostCode()).isEqualTo("POSTCODE")
        );
    }

    @Test
    void testFromStringValidWithMinimumAddressFields() {
        final var address = UnstructuredAddress.fromString("ADR++US:LINE1::::++");
        assertAll(
            () -> assertThat(address.getFormat()).isEqualTo("US"),
            () -> assertThat(address.getValue()).isEqualTo("US:LINE1::::++"),
            () -> assertThat(address.getAddressLines()).containsExactly("LINE1", "", "", "", ""),
            () -> assertThat(address.getPostCode()).isEmpty()
        );
    }

    @Test
    void testFromStringValidWithOnlyPostcode() {
        final var address = UnstructuredAddress.fromString("ADR++++POSTCODE");
        assertAll(
            () -> assertThat(address.getFormat()).isEmpty(),
            () -> assertThat(address.getValue()).isEqualTo("++POSTCODE"),
            () -> assertThat(address.getAddressLines()).isNull(),
            () -> assertThat(address.getPostCode()).isEqualTo("POSTCODE")
        );
    }

    @Test
    void testPreValidateInvalidMissingAddressAndPostcode() {
        final var address = UnstructuredAddress.fromString("ADR++++");
        assertThatThrownBy(address::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines is required when postcode is missing");
    }

    @Test
    void testPreValidateInvalidInsufficientAddressLines() {
        final var address = UnstructuredAddress.fromString("ADR++US++");
        assertThatThrownBy(address::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines is required when postcode is missing");
    }

    @Test
    void testPreValidateInvalidMissingFormat() {
        final var address = UnstructuredAddress.fromString("ADR++:LINE1::::++");
        assertThatThrownBy(address::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: format of 'US' is required when postCode is missing");
    }

    @Test
    void testPreValidateInvalidMissingPostcodeAndLine1() {
        final var address = UnstructuredAddress.fromString("ADR++US::LINE2:::++");
        assertThatThrownBy(address::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines[0] is required");
    }
}
