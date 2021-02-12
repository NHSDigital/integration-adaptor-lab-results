package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.PractitionerMapper;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private Message message;

    @InjectMocks
    private EdifactToFhirService service;

    @Test
    void testConvertEdifactToFhirRequesterMapperReturnsEmpty() {
        when(practitionerMapper.mapRequester(message)).thenReturn(Optional.empty());

        final Parameters parameters = service.convertToFhir(message);

        assertThat(parameters).isNotNull();
        assertThat(parameters.getParameter()).isEmpty();
    }

    @Test
    void testConvertEdifactToFhirRequesterMapperReturnsSomething() {
        when(practitionerMapper.mapRequester(message)).thenReturn(Optional.of(mock(Practitioner.class)));

        final Parameters parameters = service.convertToFhir(message);

        assertThat(parameters).isNotNull();
        assertThat(parameters.getParameter())
            .hasSize(1)
            .first()
            .extracting(Parameters.ParametersParameterComponent::getResource)
            .isNotNull();
    }
}
