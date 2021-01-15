package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.hl7.fhir.dstu3.model.Parameters;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

public class NotSupportedFhirTransactionMapper implements FhirTransactionMapper {

    private final TransactionType transactionType;

    public NotSupportedFhirTransactionMapper(final TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public Parameters map(final Transaction transaction) {
        throw new UnsupportedOperationException("Transaction type " + transactionType.name() + " is not supported");
    }

    @Override
    public TransactionType getTransactionType() {
        throw new UnsupportedOperationException("Transaction type " + transactionType.name() + " is not supported");
    }
}
