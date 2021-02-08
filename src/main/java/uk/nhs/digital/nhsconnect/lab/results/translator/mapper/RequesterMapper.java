package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import java.util.Optional;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;

@Component
public class RequesterMapper {
    protected static final String SDS_USER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public Optional<Practitioner> map(final Message message) {
        return message.getRequesterNameAndAddress()
            .map(this::toPractitioner);
    }

    private Practitioner toPractitioner(final RequesterNameAndAddress requester) {
        final var result = new Practitioner();
        result.addIdentifier()
            .setValue(requester.getIdentifier())
            .setSystem(SDS_USER_SYSTEM);
        result.addName()
            .setText(requester.getRequesterName());
        return result;
    }
}
