package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
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
    private SpecimenMapper specimenMapper;

    @Mock
    private ObservationMapper observationMapper;

    @Mock
    private DiagnosticReportMapper diagnosticReportMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @BeforeEach
    void setUp() {
        when(practitionerMapper.mapToRequestingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(practitionerMapper.mapToPerformingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(patientMapper.mapToPatient(any(Message.class))).thenReturn(new Patient());
        when(specimenMapper.mapToSpecimens(any(Message.class), any(Patient.class))).thenReturn(Collections.emptyList());
        when(observationMapper.mapToTestGroupsAndResults(any(Message.class))).thenReturn(Collections.emptyList());
        when(diagnosticReportMapper.mapToDiagnosticReport(nullable(Message.class), nullable(Patient.class), anyList(),
            anyList(), nullable(Practitioner.class), nullable(Organization.class))).thenReturn(new DiagnosticReport());
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
    void testMapMessageToPathologyRecordWithSpecimens() {
        final Message message = new Message(emptyList());
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        reset(specimenMapper);
        when(specimenMapper.mapToSpecimens(eq(message), any(Patient.class)))
            .thenReturn(List.of(mockSpecimen1, mockSpecimen2));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getSpecimens())
            .containsExactly(mockSpecimen1, mockSpecimen2);
    }

    @Test
    void testMapMessageToPathologyRecordWithTestResults() {
        final Message message = new Message(emptyList());
        final var mockObservation1 = mock(Observation.class);
        final var mockObservation2 = mock(Observation.class);
        when(observationMapper.mapToTestGroupsAndResults(message))
            .thenReturn(List.of(mockObservation1, mockObservation2));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getTestResults())
            .containsExactly(mockObservation1, mockObservation2);
    }

    @Test
    void testMapMessageToPathologyRecordWithDiagnosticReport() {
        final Message message = new Message(emptyList());
        final var diagnosticReport = mock(DiagnosticReport.class);
        reset(diagnosticReportMapper);
        when(diagnosticReportMapper.mapToDiagnosticReport(eq(message), nullable(Patient.class), anyList(), anyList(),
            nullable(Practitioner.class), nullable(Organization.class))).thenReturn(diagnosticReport);

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getTestReport()).isEqualTo(diagnosticReport);
    }
}
