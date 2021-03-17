package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
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
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalReportMapperTest {

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

    @Mock
    private ObservationMapper observationMapper;

    @Mock
    private DiagnosticReportMapper diagnosticReportMapper;

    @InjectMocks
    private MedicalReportMapper medicalReportMapper;

    @BeforeEach
    void setUp() {
        when(practitionerMapper.mapToRequestingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(practitionerMapper.mapToPerformingPractitioner(any(Message.class))).thenReturn(Optional.empty());
        when(patientMapper.mapToPatient(any(Message.class))).thenReturn(new Patient());
        when(procedureRequestMapper.mapToProcedureRequest(any(Message.class), any(Patient.class),
            nullable(Practitioner.class), nullable(Organization.class),
            nullable(Practitioner.class), nullable(Organization.class)))
            .thenReturn(Optional.empty());
        when(specimenMapper.mapToSpecimensBySequenceNumber(any(Message.class), any(Patient.class)))
            .thenReturn(Collections.emptyMap());
        when(observationMapper.mapToObservations(any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(diagnosticReportMapper.mapToDiagnosticReport(nullable(Message.class), nullable(Patient.class), anyList(),
            anyList(), nullable(Practitioner.class), nullable(Organization.class), nullable(ProcedureRequest.class)))
            .thenReturn(new DiagnosticReport());
    }

    @Test
    void testMapMessageToMedicalReportWithPatient() {
        final Message message = new Message(emptyList());
        var mockPatient = mock(Patient.class);
        when(patientMapper.mapToPatient(message)).thenReturn(mockPatient);

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getPatient()).isEqualTo(mockPatient);
    }

    @Test
    void testMapMessageToMedicalReportWithRequester() {
        final Message message = new Message(emptyList());
        var mockRequestingPractitioner = mock(Practitioner.class);

        when(practitionerMapper.mapToRequestingPractitioner(message))
            .thenReturn(Optional.of(mockRequestingPractitioner));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getRequestingPractitioner()).isEqualTo(mockRequestingPractitioner);
    }

    @Test
    void testMapMessageToMedicalReportWithRequestingOrganization() {
        final Message message = new Message(emptyList());
        var mockRequestingOrganization = mock(Organization.class);

        when(organizationMapper.mapToRequestingOrganization(message))
            .thenReturn(Optional.of(mockRequestingOrganization));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getRequestingOrganization()).isEqualTo(mockRequestingOrganization);
    }

    @Test
    void testMapMessageToMedicalReportWithPerformer() {
        final Message message = new Message(emptyList());
        var mockPerformingPractitioner = mock(Practitioner.class);

        when(practitionerMapper.mapToPerformingPractitioner(message))
            .thenReturn(Optional.of(mockPerformingPractitioner));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getPerformingPractitioner()).isEqualTo(mockPerformingPractitioner);
    }

    @Test
    void testMapMessageToMedicalReportWithPerformingOrganization() {
        final Message message = new Message(emptyList());
        var mockPerformingOrganization = mock(Organization.class);

        when(organizationMapper.mapToPerformingOrganization(message))
            .thenReturn(Optional.of(mockPerformingOrganization));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getPerformingOrganization()).isEqualTo(mockPerformingOrganization);
    }

    @Test
    void testMapMessageToMedicalReportWithProcedureRequest() {
        final Message message = new Message(emptyList());
        var mockProcedureRequest = mock(ProcedureRequest.class);
        reset(procedureRequestMapper);
        when(procedureRequestMapper.mapToProcedureRequest(eq(message), any(Patient.class),
            nullable(Practitioner.class), nullable(Organization.class),
            nullable(Practitioner.class), nullable(Organization.class)))
            .thenReturn(Optional.of(mockProcedureRequest));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getTestRequestSummary()).isEqualTo(mockProcedureRequest);
    }

    @Test
    void testMapMessageToMedicalReportWithSpecimens() {
        final Message message = new Message(emptyList());
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        reset(specimenMapper);
        when(specimenMapper.mapToSpecimensBySequenceNumber(eq(message), any(Patient.class)))
            .thenReturn(Map.of("1", mockSpecimen1, "2", mockSpecimen2));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getSpecimens())
            .containsOnly(mockSpecimen1, mockSpecimen2);
    }

    @Test
    void testMapMessageToMedicalReportWithTestResults() {
        final Message message = new Message(emptyList());
        final var mockObservation1 = mock(Observation.class);
        final var mockObservation2 = mock(Observation.class);
        reset(observationMapper);
        when(observationMapper.mapToObservations(eq(message), nullable(Patient.class), anyMap(),
            nullable(Organization.class), nullable(Practitioner.class)))
            .thenReturn(List.of(mockObservation1, mockObservation2));

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getTestResults())
            .containsExactly(mockObservation1, mockObservation2);
    }

    @Test
    void testMapMessageToMedicalReportWithDiagnosticReport() {
        final Message message = new Message(emptyList());
        final var diagnosticReport = mock(DiagnosticReport.class);
        reset(diagnosticReportMapper);
        when(diagnosticReportMapper.mapToDiagnosticReport(eq(message), nullable(Patient.class), anyList(), anyList(),
            nullable(Practitioner.class), nullable(Organization.class), nullable(ProcedureRequest.class)))
            .thenReturn(diagnosticReport);

        final var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertThat(medicalReport.getTestReport()).isEqualTo(diagnosticReport);
    }
}
