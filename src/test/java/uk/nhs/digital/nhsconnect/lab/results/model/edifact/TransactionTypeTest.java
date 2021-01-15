package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionTypeTest {

    @Test
    void testFromCodeForValidCodeReturnsTransactionType() {
        final ImmutableMap<String, TransactionType> codeMap = ImmutableMap.of("123", Inbound.STUB);

        assertEquals(Inbound.values().length, codeMap.size());

        codeMap.forEach((code, transactionType) -> assertEquals(transactionType, TransactionType.fromCode(code)));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromCode("INVALID"));
    }
}
