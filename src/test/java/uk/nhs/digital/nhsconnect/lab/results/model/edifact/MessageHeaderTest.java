package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageHeaderTest {

    @Test
    public void testValidMessageHeader() throws EdifactValidationException {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setSequenceNumber(2L);

        String edifact = messageHeader.toEdifact();

        assertEquals("UNH+00000002+FHSREG:0:1:FH:FHS001'", edifact);
    }

    @Test
    public void testValidationStateful() {
        MessageHeader messageHeader = new MessageHeader();

        assertThatThrownBy(messageHeader::validateStateful)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber is required");
    }

    @Test
    public void testValidationStatefulMinMaxSequenceNumber() throws EdifactValidationException {
        var messageHeader = new MessageHeader();

        messageHeader.setSequenceNumber(0L);
        assertThatThrownBy(messageHeader::validateStateful)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");

        messageHeader.setSequenceNumber(MessageHeader.MAX_MESSAGE_SEQUENCE + 1);
        assertThatThrownBy(messageHeader::validateStateful)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");

        messageHeader.setSequenceNumber(1L);
        messageHeader.validateStateful();

        messageHeader.setSequenceNumber(MessageHeader.MAX_MESSAGE_SEQUENCE);
        messageHeader.validateStateful();
    }

    @Test
    void testFromString() {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setSequenceNumber(2L);

        assertThat(MessageHeader.fromString("UNH+00000002+FHSREG:0:1:FH:FHS001").getValue()).isEqualTo(messageHeader.getValue());
        assertThatThrownBy(() -> MessageHeader.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
