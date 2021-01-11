package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.hl7.fhir.r4.model.Parameters;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;

public class NotSupportedFhirTransactionMapper implements FhirTransactionMapper {

    private final ReferenceTransactionType.TransactionType transactionType;

    public NotSupportedFhirTransactionMapper(ReferenceTransactionType.TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public Parameters map(Transaction transaction) {
        throw new UnsupportedOperationException("Transaction type " + transactionType.name() + " is not supported");
    }

    @Override
    public ReferenceTransactionType.TransactionType getTransactionType() {
        throw new UnsupportedOperationException("Transaction type " + transactionType.name() + " is not supported");
    }
}
