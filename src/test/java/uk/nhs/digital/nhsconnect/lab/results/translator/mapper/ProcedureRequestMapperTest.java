package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcedureRequestMapperTest {
    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private ProcedureRequestMapper procedureRequestMapper;

    @Mock
    private Message message;

    @Test
    void testMapMessageToProcedureRequestNoPatientClinicalInfo() {
        when(message.getPatientClinicalInfo()).thenReturn(Optional.empty());

        assertThat(procedureRequestMapper.mapToProcedureRequest(message)).isEmpty();
    }
}
