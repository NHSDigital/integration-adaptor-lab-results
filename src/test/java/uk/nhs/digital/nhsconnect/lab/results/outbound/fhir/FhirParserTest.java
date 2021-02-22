package uk.nhs.digital.nhsconnect.lab.results.outbound.fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FhirParserTest {

    private final FhirParser fhirParser = new FhirParser();

    @Test
    void encodeFhirBundleToString() {

        final String actualEncodedString = fhirParser.encodeToString(new Bundle());

        final String expectedEncodedString = "{\n  \"resourceType\": \"Bundle\"\n}";

        assertEquals(expectedEncodedString, actualEncodedString);
    }

}
