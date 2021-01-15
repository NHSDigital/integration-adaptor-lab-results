package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReferenceTransactionNumberTest {

    @Test
    void testToEdifactForValidTransactionNumber() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber(1_234L);

        final String edifact = referenceTransactionNumber.toEdifact();

        assertEquals("RFF+TN:1234'", edifact);
    }

    @Test
    void testToEdifactForInvalidTransactionNumberThrowsException() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                referenceTransactionNumber::toEdifact);

        assertEquals("RFF: Attribute transactionNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulTransactionNumberNullThrowsException() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                referenceTransactionNumber::validateStateful);

        assertEquals("RFF: Attribute transactionNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulTransactionNumberLessThanMinimumValueThrowsException() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber(0L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                referenceTransactionNumber::validateStateful);

        assertEquals("RFF: Attribute transactionNumber must be between 1 and 9999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulTransactionNumberMoreThanMaxValueThrowsException() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber(10_000_000L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                referenceTransactionNumber::validateStateful);

        assertEquals("RFF: Attribute transactionNumber must be between 1 and 9999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulTransactionNumberWithinMinMaxDoesNotThrowException() {
        final ReferenceTransactionNumber referenceTransactionNumber = new ReferenceTransactionNumber(9_999_999L);

        assertDoesNotThrow(referenceTransactionNumber::validateStateful);
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsReferenceTransactionNumber() {
        final ReferenceTransactionNumber referenceTransactionNumber = ReferenceTransactionNumber.fromString("RFF+TN:1234");

        assertEquals("RFF", referenceTransactionNumber.getKey());
        assertEquals("TN:1234", referenceTransactionNumber.getValue());
        assertEquals(1234L, referenceTransactionNumber.getTransactionNumber());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> ReferenceTransactionNumber.fromString("wrong value"));

        assertEquals("Can't create ReferenceTransactionNumber from wrong value", exception.getMessage());
    }
}
