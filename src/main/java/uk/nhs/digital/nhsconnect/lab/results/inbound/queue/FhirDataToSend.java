package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Bundle;

@Getter
@Setter
public class FhirDataToSend {

    private Bundle content;
    private String operationId;
}
