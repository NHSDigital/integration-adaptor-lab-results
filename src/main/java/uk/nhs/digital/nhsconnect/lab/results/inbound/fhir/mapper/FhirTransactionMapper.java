package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.hl7.fhir.dstu3.model.Parameters;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

public interface FhirTransactionMapper {
    Parameters map(Transaction transaction);

    TransactionType getTransactionType();

}
