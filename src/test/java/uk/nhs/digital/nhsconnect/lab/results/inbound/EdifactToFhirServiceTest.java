package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.FhirTransactionMapper;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper.StubMapper;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Inbound;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceTransactionType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    @Mock
    private Transaction transaction;
    @Mock
    private Message message;

    @Test
    void testConvertEdifactToFhir() {

        final Map<TransactionType, FhirTransactionMapper> transactionMapper = Collections.singletonMap(Inbound.STUB, new StubMapper());
        final EdifactToFhirService service = new EdifactToFhirService(transactionMapper);

        when(transaction.getMessage()).thenReturn(message);
        when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(Inbound.STUB));

        final Parameters parameters = service.convertToFhir(transaction);

        assertNotNull(parameters);
    }

    @Test
    void testConvertEdifactToFhirWithUnsupportedTransactionTypeThrowsException() {

        final EdifactToFhirService service = new EdifactToFhirService(Map.of());

        when(transaction.getMessage()).thenReturn(message);
        when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(Inbound.STUB));

        final UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> service.convertToFhir(transaction));

        assertEquals("Transaction type STUB is not supported", exception.getMessage());
    }

}
