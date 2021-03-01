package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateRequester;

@ExtendWith(MockitoExtension.class)
class PathologyRecordMapperTest {

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private ProcedureRequestMapper procedureRequestMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @Test
    void testMapMessageToPathologyRecord() {
        final Message message = new Message(new ArrayList<>());

        when(practitionerMapper.mapRequester(message)).thenReturn(
                Optional.of(generateRequester("Dr Bob Hope", AdministrativeGender.MALE))
        );

        when(procedureRequestMapper.mapToProcedureRequest(message)).thenReturn(
            Optional.empty()
        );

        final PathologyRecord pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        Practitioner requester = pathologyRecord.getRequester();

        assertAll(
            () -> assertThat(requester.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Dr Bob Hope"),
            () -> assertThat(requester.getGender().toCode())
                    .isEqualTo("male")
        );
    }
}
