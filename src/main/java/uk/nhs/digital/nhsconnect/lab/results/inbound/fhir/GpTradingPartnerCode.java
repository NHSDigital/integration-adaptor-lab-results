package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import ca.uhn.fhir.model.api.annotation.Block;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.ParameterNames;

@Block()
public class GpTradingPartnerCode extends Parameters.ParametersParameterComponent {
    public GpTradingPartnerCode(Interchange interchange) {
        super();
        String recipient = interchange.getInterchangeHeader().getRecipient();
        this.setName(ParameterNames.GP_TRADING_PARTNER_CODE);
        this.setValue(new StringType(recipient));
    }

    public GpTradingPartnerCode(String value) {
        super();
        this.setName(ParameterNames.GP_TRADING_PARTNER_CODE);
        this.setValue(new StringType(value));
    }
}
