package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PartnerAgreedIdentificationTest {
    @Test
    void testGetKey() {
        final var agreedId = new PartnerAgreedIdentification("");
        assertThat(agreedId.getKey()).isEqualTo("RFF");
    }

    @Test
    void testGetValue() {
        final var agreedId = new PartnerAgreedIdentification("reference");
        assertThat(agreedId.getValue()).isEqualTo("AHI:reference");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> PartnerAgreedIdentification.fromString("WRONG+AHI:ref"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create PartnerAgreedIdentification from WRONG+AHI:ref");
    }

    @Test
    void testFromStringAllFields() {
        final var agreedId = PartnerAgreedIdentification.fromString("RFF+AHI:BestGP");
        assertThat(agreedId.getReference()).isEqualTo("BestGP");
    }

    @Test
    void testValidationPasses() {
        final var agreedId = new PartnerAgreedIdentification("something");
        assertAll(
            () -> assertDoesNotThrow(agreedId::validateStateful),
            () -> assertDoesNotThrow(agreedId::preValidate)
        );
    }

    @Test
    void testValidationFailsBlankReference() {
        final var agreedId = new PartnerAgreedIdentification(" \t ");
        assertAll(
            () -> assertDoesNotThrow(agreedId::validateStateful),
            () -> assertThatThrownBy(agreedId::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: Attribute reference is required")
        );
    }

    @Test
    void testValidationFailsEmptyReference() {
        final var agreedId = new PartnerAgreedIdentification("");
        assertAll(
            () -> assertDoesNotThrow(agreedId::validateStateful),
            () -> assertThatThrownBy(agreedId::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: Attribute reference is required")
        );
    }
}
