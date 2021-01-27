package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {
    @Test
    void testGetMessageHeader() {
        final Message msg = new Message(List.of(
            "UNH+00000009+FHSREG:0:1:FH:FHS001'"
        ));

        MessageHeader header = msg.getMessageHeader();

        assertAll(
            () -> assertEquals("UNH", header.getKey()),
            () -> assertEquals("00000009+FHSREG:0:1:FH:FHS001", header.getValue())
        );
    }

    @Test
    void testGetReferenceTransactionType() {
        final Message msg = new Message(List.of(
            "RFF+950:F4"
        ));

        ReferenceTransactionType type = msg.getReferenceTransactionType();

        assertAll(
            () -> assertEquals("RFF", type.getKey()),
            () -> assertEquals("950:F4", type.getValue()),
            () -> assertEquals(TransactionType.APPROVAL, type.getTransactionType())
        );
    }

    @Test
    void testGetHealthAuthorityNameAndAddress() {
        final Message msg = new Message(List.of(
            "NAD+FHS+XX1:954'"
        ));

        HealthAuthorityNameAndAddress haAddress = msg.getHealthAuthorityNameAndAddress();

        assertAll(
            () -> assertEquals("NAD", haAddress.getKey()),
            () -> assertEquals("FHS+XX1:954'", haAddress.getValue()),
            () -> assertEquals("954'", haAddress.getCode()),
            () -> assertEquals("XX1", haAddress.getIdentifier())
        );
    }

    @Test
    void testFindFirstGpCodeDefaultsTo9999() {
        final Message msg = new Message(List.of());
        msg.setTransactions(List.of(
            new Transaction(Collections.emptyList())
        ));

        String firstGpCode = msg.findFirstGpCode();

        assertEquals("9999", firstGpCode);
    }

    @Test
    void testFindFirstGpCodeReturnsCorrectly() {
        final Message msg = new Message(List.of());
        msg.setTransactions(List.of(
            new Transaction(List.of(
                "NAD+GP+2750922,295:900'",
                "NAD+GP+1649811,184:899'"
            ))
        ));

        String firstGpCode = msg.findFirstGpCode();

        assertEquals("2750922,295", firstGpCode);
    }
}
