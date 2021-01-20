package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InboundEdifactTransactionHandler {

    private final EdifactToFhirService edifactToFhirService;

    public DataToSend translate(final Transaction transaction) {
        final Parameters parameters = edifactToFhirService.convertToFhir(transaction);

        final DataToSend dataToSend = new DataToSend();
        dataToSend.setContent(parameters);

        return dataToSend;
    }
}
