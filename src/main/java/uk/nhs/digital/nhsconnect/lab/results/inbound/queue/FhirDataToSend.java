package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Parameters;

@Getter
@Setter
public class FhirDataToSend {
    private Object content;
    private String operationId;

    public FhirDataToSend setContent(final Parameters parameters) {
        this.content = parameters;
        return this;
    }
}
