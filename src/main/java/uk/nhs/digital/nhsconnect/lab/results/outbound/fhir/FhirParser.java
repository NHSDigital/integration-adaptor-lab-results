package uk.nhs.digital.nhsconnect.lab.results.outbound.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;

@Component
public class FhirParser {

    private final IParser parser;

    public FhirParser() {
        FhirContext ctx = FhirContext.forDstu3();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        parser = ctx.newJsonParser();
    }

    public String encodeToString(IBaseResource resource) {
        return parser.setPrettyPrint(true).encodeResourceToString(resource);
    }
}

