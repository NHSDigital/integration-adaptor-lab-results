package uk.nhs.digital.nhsconnect.lab.results.model.fhir;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import org.hl7.fhir.r4.model.Identifier;

@DatatypeDef(name = "Identifier")
public class ManagingOrganizationIdentifier extends Identifier {
    private static final String SYSTEM = "https://digital.nhs.uk/services/nhais/guide-to-nhais-gp-links-documentation";

    public ManagingOrganizationIdentifier(String organizationId) {
        super();
        this.setSystem(SYSTEM);
        this.setValue(organizationId);
    }
}
