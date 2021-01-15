package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.FhirTransactionMapper;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.NotSupportedFhirTransactionMapper;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EdifactToFhirService {

    private final Map<TransactionType, FhirTransactionMapper> transactionMappers;

    public Parameters convertToFhir(final Transaction transaction) {

        final Message message = transaction.getMessage();
        final ReferenceTransactionType referenceTransactionType = message.getReferenceTransactionType();
        final TransactionType transactionType = referenceTransactionType.getTransactionType();

        return transactionMappers
                .getOrDefault(transactionType, new NotSupportedFhirTransactionMapper(transactionType))
                .map(transaction);
    }

}
