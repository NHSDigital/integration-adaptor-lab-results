package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class MessageTypeTest {

    @Test
    void testFromCodeReturnsMessageTypeForValidString() {
        assertAll(
            () -> assertThat(MessageType.fromCode("NHS001")).isEqualTo(MessageType.NHSACK),
            () -> assertThat(MessageType.fromCode("NHS002")).isEqualTo(MessageType.PATHOLOGY_VARIANT_2),
            () -> assertThat(MessageType.fromCode("NHS003")).isEqualTo(MessageType.PATHOLOGY_VARIANT_3),
            () -> assertThat(MessageType.fromCode("NHS004")).isEqualTo(MessageType.SCREENING)
        );
    }

    @Test
    void testFromCodeThrowsExceptionForInvalidString() {
        assertThatIllegalArgumentException().isThrownBy(() -> MessageType.fromCode("INVALID"))
            .withMessage("No message type for \"INVALID\"");
    }
}
