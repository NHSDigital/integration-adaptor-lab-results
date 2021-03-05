package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
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
    private PatientMapper patientMapper;

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private ProcedureRequestMapper procedureRequestMapper;

    @Mock
    private SpecimenMapper specimenMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @BeforeEach
    void setUp() {
        when(practitionerMapper.mapToRequestingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(practitionerMapper.mapToPerformingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(patientMapper.mapToPatient(any(Message.class))).thenReturn(new Patient());
        when(procedureRequestMapper.mapToProcedureRequest(any(Message.class))).thenReturn(Optional.empty());
        when(specimenMapper.mapToSpecimens(any(Message.class), any(Patient.class))).thenReturn(Collections.emptyList());
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
    void testMapMessageToPathologyRecordWithRequester() {
        final Message message = new Message(emptyList());
        var mockRequestingPractitioner = mock(Practitioner.class);

        when(practitionerMapper.mapToRequestingPractitioner(message))
            .thenReturn(Optional.of(mockRequestingPractitioner));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getRequestingPractitioner()).isEqualTo(mockRequestingPractitioner);
    }

    @Test
    void testMapMessageToPathologyRecordWithRequestingOrganization() {
        final Message message = new Message(emptyList());
        var mockRequestingOrganization = mock(Organization.class);

        when(organizationMapper.mapToRequestingOrganization(message))
            .thenReturn(Optional.of(mockRequestingOrganization));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getRequestingOrganization()).isEqualTo(mockRequestingOrganization);
    }

    @Test
    void testMapMessageToPathologyRecordWithPerformer() {
        final Message message = new Message(emptyList());
        var mockPerformingPractitioner = mock(Practitioner.class);

        when(practitionerMapper.mapToPerformingPractitioner(message))
            .thenReturn(Optional.of(mockPerformingPractitioner));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getPerformingPractitioner()).isEqualTo(mockPerformingPractitioner);
    }

    @Test
    void testMapMessageToPathologyRecordWithPerformingOrganization() {
        final Message message = new Message(emptyList());
        var mockPerformingOrganization = mock(Organization.class);

        when(organizationMapper.mapToPerformingOrganization(message))
            .thenReturn(Optional.of(mockPerformingOrganization));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getPerformingOrganization()).isEqualTo(mockPerformingOrganization);
    }

    @Test
    void testMapMessageToPathologyRecordWithProcedureRequest() {
        final Message message = new Message(emptyList());
        var mockProcedureRequest = mock(ProcedureRequest.class);

        when(procedureRequestMapper.mapToProcedureRequest(message)).thenReturn(Optional.of(mockProcedureRequest));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getTestRequestSummary()).isEqualTo(mockProcedureRequest);
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
