package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import com.google.common.collect.Lists;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.MessageHeader;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalReportMapperTest {

    private static final String SPECIMEN_1_SEQUENCE_NUMBER = "1";
    private static final String SPECIMEN_2_SEQUENCE_NUMBER = "2";

    @Mock
    private Message message;

    @Mock
    private MessageHeaderMapper messageHeaderMapper;

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

    @Test
    void when_mappingMessage_expect_medicalReportWithAllResourcesIsReturned() {
        var messageHeader = mock(MessageHeader.class);
        var patient = mock(Patient.class);
        var diagnosticReport = mock(DiagnosticReport.class);
        var performingOrganization = mock(Organization.class);
        var performingPractitioner = mock(Practitioner.class);
        var requestingOrganization = mock(Organization.class);
        var requestingPractitioner = mock(Practitioner.class);
        var procedureRequest = mock(ProcedureRequest.class);
        var specimen1 = mock(Specimen.class);
        var specimen2 = mock(Specimen.class);
        var observation1 = mock(Observation.class);
        var observation2 = mock(Observation.class);

        var observations = List.of(observation1, observation2);
        var specimensBySequenceNumber = Map.of(
            SPECIMEN_1_SEQUENCE_NUMBER, specimen1,
            SPECIMEN_2_SEQUENCE_NUMBER, specimen2
        );
        var specimens = Lists.newArrayList(specimensBySequenceNumber.values());

        when(messageHeaderMapper.mapToMessageHeader(
                message, diagnosticReport, performingOrganization, requestingOrganization))
            .thenReturn(messageHeader);
        when(patientMapper.mapToPatient(message)).thenReturn(patient);
        when(organizationMapper.mapToRequestingOrganization(message)).thenReturn(requestingOrganization);
        when(practitionerMapper.mapToRequestingPractitioner(message)).thenReturn(requestingPractitioner);
        when(organizationMapper.mapToPerformingOrganization(message)).thenReturn(performingOrganization);
        when(practitionerMapper.mapToPerformingPractitioner(message)).thenReturn(performingPractitioner);
        when(procedureRequestMapper.mapToProcedureRequest(
                message, patient, requestingPractitioner, requestingOrganization, performingPractitioner))
            .thenReturn(procedureRequest);
        when(specimenMapper.mapToSpecimensBySequenceNumber(message, patient))
            .thenReturn(specimensBySequenceNumber);
        when(observationMapper.mapToObservations(
                message, patient, specimensBySequenceNumber, performingOrganization, performingPractitioner))
            .thenReturn(observations);
        when(diagnosticReportMapper.mapToDiagnosticReport(
                message, patient, specimens, observations,
                performingPractitioner, performingOrganization, procedureRequest))
            .thenReturn(diagnosticReport);

        var medicalReport = medicalReportMapper.mapToMedicalReport(message);

        assertAll(
            () -> assertThat(medicalReport.getMessageHeader()).isEqualTo(messageHeader),
            () -> assertThat(medicalReport.getPatient()).isEqualTo(patient),
            () -> assertThat(medicalReport.getPerformingOrganization()).isEqualTo(performingOrganization),
            () -> assertThat(medicalReport.getPerformingPractitioner()).isEqualTo(performingPractitioner),
            () -> assertThat(medicalReport.getRequestingOrganization()).isEqualTo(requestingOrganization),
            () -> assertThat(medicalReport.getRequestingPractitioner()).isEqualTo(requestingPractitioner),
            () -> assertThat(medicalReport.getTestReport()).isEqualTo(diagnosticReport),
            () -> assertThat(medicalReport.getTestRequestSummary()).isEqualTo(procedureRequest),
            () -> assertThat(medicalReport.getSpecimens()).isEqualTo(specimens),
            () -> assertThat(medicalReport.getTestResults()).isEqualTo(observations)
        );
    }
}
