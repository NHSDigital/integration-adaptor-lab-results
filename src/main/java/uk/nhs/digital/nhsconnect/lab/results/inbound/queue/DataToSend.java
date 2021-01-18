package uk.nhs.digital.nhsconnect.lab.results.inbound.queue;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Parameters;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

public class DataToSend {

    @Getter
    private Object content;
    @Getter
    @Setter
    private String operationId;
    @Getter
    @Setter
    private TransactionType transactionType;

    public DataToSend setContent(final Parameters parameters) {
        this.content = parameters;
        return this;
    }
}
