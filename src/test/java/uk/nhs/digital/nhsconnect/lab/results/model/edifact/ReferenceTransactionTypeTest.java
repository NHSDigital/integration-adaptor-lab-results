package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReferenceTransactionTypeTest {

    @Test
    void testToEdifactForValidTransactionType() {
        final ReferenceTransactionType referenceTransactionType = new ReferenceTransactionType(Inbound.STUB);

        final String edifact = referenceTransactionType.toEdifact();

        assertEquals("RFF+950:123'", edifact);
    }

    @Test
    void testToEdifactForInvalidTransactionTypeThrowsException() {
        final ReferenceTransactionType referenceTransactionType = new ReferenceTransactionType();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                referenceTransactionType::toEdifact);

        assertEquals("RFF: Attribute transactionType is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsReferenceTransactionType() {
        final ReferenceTransactionType referenceTransactionType = ReferenceTransactionType.fromString("RFF+950:123");

        assertEquals("RFF", referenceTransactionType.getKey());
        assertEquals("950:123", referenceTransactionType.getValue());
        assertEquals(TransactionType.fromCode("123"), referenceTransactionType.getTransactionType());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> ReferenceTransactionType.fromString("wrong value"));

        assertEquals("Can't create ReferenceTransactionType from wrong value", exception.getMessage());
    }

}
