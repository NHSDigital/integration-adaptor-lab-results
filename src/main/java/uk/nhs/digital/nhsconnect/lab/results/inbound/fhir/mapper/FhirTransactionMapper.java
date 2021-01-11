package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.hl7.fhir.r4.model.Parameters;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.GpTradingPartnerCode;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.PatientParameter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;

public interface FhirTransactionMapper {
    Parameters map(Transaction transaction);

    ReferenceTransactionType.TransactionType getTransactionType();

    static Parameters createParameters(Transaction transaction) {
        return new Parameters()
            .addParameter(new GpTradingPartnerCode(transaction.getMessage().getInterchange()))
            .addParameter(new PatientParameter(transaction));
    }

}
