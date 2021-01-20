package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReferenceTransactionNumberTest {

    private ReferenceTransactionNumber referenceTransactionNumber;

    @BeforeEach
    public void setUp() {
        referenceTransactionNumber = new ReferenceTransactionNumber();
    }


    @Test
    public void testValidReferenceTransactionType() throws EdifactValidationException {
        referenceTransactionNumber.setTransactionNumber(1234L);
        String edifact = referenceTransactionNumber.toEdifact();

        assertEquals("RFF+TN:1234'", edifact);
    }

    @Test
    public void testValidationStatefulMinMaxTransactionNumber() throws EdifactValidationException {
        referenceTransactionNumber.setTransactionNumber(0L);
        assertThatThrownBy(referenceTransactionNumber::validateStateful)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute transactionNumber must be between 1 and 9999999");

        referenceTransactionNumber.setTransactionNumber(10_000_000L);
        assertThatThrownBy(referenceTransactionNumber::validateStateful)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute transactionNumber must be between 1 and 9999999");

        referenceTransactionNumber.setTransactionNumber(1L);
        referenceTransactionNumber.validateStateful();

        referenceTransactionNumber.setTransactionNumber(9_999_999L);
        referenceTransactionNumber.validateStateful();
    }

    @Test
    void testFromString() {
        referenceTransactionNumber.setTransactionNumber(1234L);

        assertThat(referenceTransactionNumber.fromString("RFF+TN:1234").getValue()).isEqualTo(referenceTransactionNumber.getValue());
        assertThatThrownBy(() -> referenceTransactionNumber.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
