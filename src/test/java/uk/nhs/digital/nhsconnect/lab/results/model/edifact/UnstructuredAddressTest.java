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
            () -> assertThat(address.getAddressLines())
                .containsExactly(new String[]{"LINE1", "LINE2", "LINE3", "LINE4", "LINE5"}),
            () -> assertThat(address.getPostCode()).contains("POSTCODE")
        );
    }

    @Test
    void testFromStringValidWithMinimumAddressFields() {
        final var address = UnstructuredAddress.fromString("ADR++US:LINE1::::++");
        assertAll(
            () -> assertThat(address.getFormat()).isEqualTo("US"),
            () -> assertThat(address.getAddressLines()).containsExactly(new String[]{"LINE1", "", "", "", ""}),
            () -> assertThat(address.getPostCode()).isEmpty()
        );
    }

    @Test
    void testFromStringValidWithNoPostcode() {
        final var address = UnstructuredAddress.fromString("ADR++US:SLOANE SQUARE:LONDON");
        assertAll(
            () -> assertThat(address.getFormat()).isEqualTo("US"),
            () -> assertThat(address.getAddressLines()).containsExactly(new String[]{"SLOANE SQUARE", "LONDON"}),
            () -> assertThat(address.getPostCode()).isEmpty()
        );
    }

    @Test
    void testFromStringValidWithMinimumAddressFieldsIgnoringTrailingColons() {
        final var address = UnstructuredAddress.fromString("ADR++US:LINE1++");
        assertAll(
            () -> assertThat(address.getFormat()).isEqualTo("US"),
            () -> assertThat(address.getAddressLines()).containsExactly(new String[]{"LINE1"}),
            () -> assertThat(address.getPostCode()).isEmpty()
        );
    }

    @Test
    void testFromStringValidWithOnlyPostcode() {
        final var address = UnstructuredAddress.fromString("ADR++++POSTCODE");
        assertAll(
            () -> assertThat(address.getFormat()).isEmpty(),
            () -> assertThat(address.getAddressLines()).isEmpty(),
            () -> assertThat(address.getPostCode()).contains("POSTCODE")
        );
    }

    @Test
    void testValidateInvalidMissingAddressAndPostcode() {
        final var address = UnstructuredAddress.fromString("ADR++++");
        assertThatThrownBy(address::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines is required when postcode is missing");
    }

    @Test
    void testValidateInvalidInsufficientAddressLines() {
        final var address = UnstructuredAddress.fromString("ADR++US++");
        assertThatThrownBy(address::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines is required when postcode is missing");
    }

    @Test
    void testValidateInvalidMissingFormat() {
        final var address = UnstructuredAddress.fromString("ADR++:LINE1::::++");
        assertThatThrownBy(address::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: format of 'US' is required when postCode is missing");
    }

    @Test
    void testValidateInvalidMissingPostcodeAndLine1() {
        final var address = UnstructuredAddress.fromString("ADR++US::LINE2:::++");
        assertThatThrownBy(address::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("ADR: attribute addressLines[0] is required");
    }
}
