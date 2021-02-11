package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.PractitionerMapper;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EdifactToFhirService {
    private final PractitionerMapper practitionerMapper;

    public Parameters convertToFhir(final Message message) {
        final var parameters = new Parameters();

        practitionerMapper.mapRequester(message)
            .map(practitioner -> new ParametersParameterComponent().setResource(practitioner))
            .ifPresent(parameters::addParameter);

        return parameters;
    }
}
