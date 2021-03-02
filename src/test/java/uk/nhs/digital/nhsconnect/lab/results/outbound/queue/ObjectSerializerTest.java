package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.outbound.fhir.FhirParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectSerializerTest {

    @InjectMocks
    private ObjectSerializer objectSerializer;
    @Mock
    private FhirParser fhirParser;

    @Test
    void serializeFhirObjectToString() {
        final String expected = "{\n  \"resourceType\": \"Bundle\"\n}";

        final Bundle bundle = new Bundle();
        when(fhirParser.encodeToString(bundle)).thenReturn(expected);

        assertEquals(expected, objectSerializer.serialize(bundle));
    }

    @Test
    void serializeInvalidDataTypeToStringThrowsException() {
        final UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> objectSerializer.serialize(new Object()));

        assertEquals("Data type Object is not supported", exception.getMessage());
    }
}
