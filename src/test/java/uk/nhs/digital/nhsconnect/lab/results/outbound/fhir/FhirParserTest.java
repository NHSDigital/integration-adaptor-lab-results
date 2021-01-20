package uk.nhs.digital.nhsconnect.lab.results.outbound.fhir;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FhirParserTest {

    private final FhirParser fhirParser = new FhirParser();

    @Test
    void encodeFhirParametersToString() {

        final String actualEncodedString = fhirParser.encodeToString(new Parameters());

        final String expectedEncodedString = "{\n  \"resourceType\": \"Parameters\"\n}";

        assertEquals(expectedEncodedString, actualEncodedString);
    }

}
