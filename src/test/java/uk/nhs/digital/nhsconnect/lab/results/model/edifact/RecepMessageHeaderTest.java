package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecepMessageHeaderTest {
    private RecepMessageHeader recepMessageHeader;

    @BeforeEach
    void setUp() {
        recepMessageHeader = new RecepMessageHeader();
    }

    @Test
    void testGetKey() {
        assertEquals("UNH", recepMessageHeader.getKey());
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testGetValue() {
        recepMessageHeader.setSequenceNumber(12_345L);

        assertEquals("00012345+RECEP:0:2:FH", recepMessageHeader.getValue());
    }

    @Test
    void testGetValueNullSequenceNumber() {
        assertThatThrownBy(recepMessageHeader::validateStateful)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber is required");
    }

    @Test
    void testGetValueNonPositiveSequenceNumber() {
        recepMessageHeader.setSequenceNumber(-1L);

        assertThatThrownBy(recepMessageHeader::validateStateful)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be greater than or equal to 1");
    }
}
