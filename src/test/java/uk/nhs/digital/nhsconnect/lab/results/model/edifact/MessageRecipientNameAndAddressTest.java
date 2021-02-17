package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MessageRecipientNameAndAddressTest {
    @Test
    void testGetKey() {
        final var recipient = new MessageRecipientNameAndAddress("",
            HealthcareRegistrationIdentificationCode.CONSULTANT, null);
        assertThat(recipient.getKey()).isEqualTo("NAD");
    }

    @Test
    void testGetValue() {
        final var recipient = new MessageRecipientNameAndAddress("ID",
            HealthcareRegistrationIdentificationCode.GP, "Name");
        assertThat(recipient.getValue()).isEqualTo("MR+ID:900++Name");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> MessageRecipientNameAndAddress.fromString("WRONG+MR+ID:900++Name"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create MessageRecipientNameAndAddress from WRONG+MR+ID:900++Name");
    }

    @Test
    void testFromStringAllValues() {
        final var recipient = MessageRecipientNameAndAddress.fromString("NAD+MR+ID:901++Aaron Aaronson");
        assertAll(
            () -> assertThat(recipient.getIdentifier()).isEqualTo("ID"),
            () -> assertThat(recipient.getHealthcareRegistrationIdentificationCode())
                .isEqualTo(HealthcareRegistrationIdentificationCode.GP_PRACTICE),
            () -> assertThat(recipient.getMessageRecipientName()).isEqualTo("Aaron Aaronson")
        );
    }

    @Test
    void testFromStringUnknownHealthcareRegistrationIdentificationCode() {
        assertThatThrownBy(() -> MessageRecipientNameAndAddress.fromString("NAD+MR+123456:123++Mr Blobby"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No HealthcareRegistrationIdentificationCode for '123'");
    }

    @Test
    void testValidationPasses() {
        final var recipient = MessageRecipientNameAndAddress.fromString("NAD+MR+42:902++Woody Woodpecker");
        assertAll(
            () -> assertDoesNotThrow(recipient::validateStateful),
            () -> assertDoesNotThrow(recipient::preValidate)
        );
    }

    @Test
    void testValidationBlankIdentifier() {
        final var recipient = MessageRecipientNameAndAddress.fromString("NAD+MR+:900++Wile E Coyote");
        assertAll(
            () -> assertDoesNotThrow(recipient::validateStateful),
            () -> assertThatThrownBy(recipient::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute identifier is required")
        );
    }
}
