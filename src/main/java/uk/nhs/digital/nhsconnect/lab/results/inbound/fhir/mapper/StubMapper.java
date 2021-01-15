package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Inbound;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

@Component
public class StubMapper implements FhirTransactionMapper {

    @Override
    public Parameters map(Transaction transaction) {
        return new Parameters();
    }

    @Override
    public TransactionType getTransactionType() {
        return Inbound.STUB;
    }
}
