package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    //@Mock
    //private Transaction transaction;
    @Mock
    private Message message;

    @Test
    void testConvertEdifactToFhir() {

        final EdifactToFhirService service = new EdifactToFhirService();

        //when(transaction.getMessage()).thenReturn(message);
        //when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(TransactionType.APPROVAL));

        final Parameters parameters = service.convertToFhir(message);

        assertNotNull(parameters);
    }

    /*
    @Test
    void testConvertEdifactToFhirWithUnsupportedTransactionTypeThrowsException() {

        final EdifactToFhirService service = new EdifactToFhirService(Map.of());

        when(transaction.getMessage()).thenReturn(message);
        when(message.getReferenceTransactionType()).thenReturn(new ReferenceTransactionType(TransactionType.APPROVAL));

        final UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> service.convertToFhir(transaction));

        assertEquals("Transaction type APPROVAL is not supported", exception.getMessage());
    }

     */

}
