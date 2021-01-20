package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.EdifactToFhirService;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.DataToSend;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundEdifactTransactionHandlerTest {

    @InjectMocks
    private InboundEdifactTransactionHandler inboundEdifactTransactionHandler;
    @Mock
    private EdifactToFhirService edifactToFhirService;
    @Mock
    private Transaction transaction;

    @Test
    void testTranslateEdifactTransactionToFhir() {
        final Parameters parameters = new Parameters();

        when(edifactToFhirService.convertToFhir(transaction)).thenReturn(parameters);

        final DataToSend dataToSend = inboundEdifactTransactionHandler.translate(transaction);

        assertEquals(parameters, dataToSend.getContent());
    }
}
