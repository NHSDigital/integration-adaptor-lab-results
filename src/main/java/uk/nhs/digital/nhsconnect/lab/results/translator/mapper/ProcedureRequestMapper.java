package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Component
public class ProcedureRequestMapper {
    public ProcedureRequest mapToProcedureRequest(final Message message) {
        return new ProcedureRequest();
    }
}
