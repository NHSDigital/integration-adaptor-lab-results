package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    @Mock
    private Message message;

    @Test
    void testConvertEdifactToFhir() {

        final EdifactToFhirService service = new EdifactToFhirService();

        final Parameters parameters = service.convertToFhir(message);

        assertNotNull(parameters);
    }

}
