package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathologyRecordMapperTest {

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private ProcedureRequestMapper procedureRequestMapper;

    @Mock
    private SpecimenMapper specimenMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @BeforeEach
    void setUp() {
        when(practitionerMapper.mapRequester(any(Message.class))).thenReturn(Optional.empty());
        when(practitionerMapper.mapPerformer(any(Message.class))).thenReturn(Optional.empty());
        when(patientMapper.mapToPatient(any(Message.class))).thenReturn(new Patient());
        when(specimenMapper.mapToSpecimens(any(Message.class), any(Patient.class))).thenReturn(Collections.emptyList());
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testMapMessageToPathologyRecordWithPractitioner() {
        final Message message = new Message(emptyList());
        var mockPractitioner = mock(Practitioner.class);

        when(practitionerMapper.mapRequester(message))
            .thenReturn(Optional.of(mockPractitioner));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getRequester()).isEqualTo(mockPractitioner);
    }

    @Test
    void testMapMessageToPathologyRecordWithPatient() {
        final Message message = new Message(emptyList());
        var mockPatient = mock(Patient.class);
        when(patientMapper.mapToPatient(message)).thenReturn(mockPatient);

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getPatient()).isEqualTo(mockPatient);
    }

    @Test
    void testMapMessageToPathologyRecordWithPerformer() {
        final Message message = new Message(emptyList());
        var mockPerformer = mock(Practitioner.class);

        when(practitionerMapper.mapPerformer(message)).thenReturn(Optional.of(mockPerformer));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getPerformer()).isEqualTo(mockPerformer);
    }

    @Test
    void testMapMessageToPathologyRecordWithSpecimens() {
        final Message message = new Message(emptyList());
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        reset(specimenMapper);
        when(specimenMapper.mapToSpecimens(eq(message), any(Patient.class)))
            .thenReturn(List.of(mockSpecimen1, mockSpecimen2));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getSpecimens())
            .hasSize(2)
            .contains(mockSpecimen1, mockSpecimen2);
    }
}
