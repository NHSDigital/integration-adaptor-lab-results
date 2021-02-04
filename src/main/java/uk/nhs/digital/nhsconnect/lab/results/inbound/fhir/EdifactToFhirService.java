package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EdifactToFhirService {

    public Parameters convertToFhir(final Message message) {
        //        final Message message = transaction.getMessage();
        //        final ReferenceTransactionType referenceTransactionType = message.getReferenceTransactionType();
        //        final TransactionType transactionType = referenceTransactionType.getTransactionType();
        //
        //        return transactionMappers
        //                .getOrDefault(transactionType, new NotSupportedFhirTransactionMapper(transactionType))
        //                .map(transaction);

        return new Parameters();
    }

}
