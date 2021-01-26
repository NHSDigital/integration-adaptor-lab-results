package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecepNationalHealthBodyTest {
    private RecepNationalHealthBody recepNationalHealthBody;

    @Test
    void testGetKey() {
        recepNationalHealthBody = new RecepNationalHealthBody("", "");
        assertEquals("NHS", recepNationalHealthBody.getKey());
    }

    @Test
    void testGetValue() {
        recepNationalHealthBody = new RecepNationalHealthBody("cipher", "gpCode");
        assertEquals("cipher:819:201+gpCode:814:202", recepNationalHealthBody.getValue());
    }
}
