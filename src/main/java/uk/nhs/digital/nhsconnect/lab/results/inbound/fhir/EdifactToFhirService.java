package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.FhirTransactionMapper;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.NotSupportedFhirTransactionMapper;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EdifactToFhirService {

    public final Map<ReferenceTransactionType.TransactionType, FhirTransactionMapper> transactionMappers;

    public Parameters convertToFhir(Transaction transaction) {
        var transactionType = transaction.getMessage().getReferenceTransactionType().getTransactionType();
        return transactionMappers
            .getOrDefault(transactionType, new NotSupportedFhirTransactionMapper(transactionType))
            .map(transaction);
    }
}
