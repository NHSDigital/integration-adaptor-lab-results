package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ProcedureRequestMapperTest {

    @Test
    void testMapMessageToProcedureRequest() {
        final Message message = new Message(new ArrayList<>());

        assertThat(new ProcedureRequestMapper().mapToProcedureRequest(message))
            .isExactlyInstanceOf(ProcedureRequest.class);
    }
}
