package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport.MedicalReportBuilder;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MedicalReportMapper {

    private final PatientMapper patientMapper;
    private final PractitionerMapper practitionerMapper;
    private final ProcedureRequestMapper procedureRequestMapper;
    private final OrganizationMapper organizationMapper;
    private final SpecimenMapper specimenMapper;
    private final DiagnosticReportMapper diagnosticReportMapper;
    private final ObservationMapper observationMapper;
    private final MessageHeaderMapper messageHeaderMapper;

    public MedicalReport mapToMedicalReport(final Message message) {
        final var patient = patientMapper.mapToPatient(message);
        final var requestingPractitioner = practitionerMapper.mapToRequestingPractitioner(message);
        final var requestingOrganization = organizationMapper.mapToRequestingOrganization(message);
        final var performingPractitioner = practitionerMapper.mapToPerformingPractitioner(message);
        final var performingOrganization = organizationMapper.mapToPerformingOrganization(message);
        final var specimensBySequenceNumber = specimenMapper
            .mapToSpecimensBySequenceNumber(message, patient);
        final var specimens = Lists.newArrayList(specimensBySequenceNumber.values());
        final var observations = observationMapper.mapToObservations(
            message, patient, specimensBySequenceNumber, performingOrganization, performingPractitioner);
        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, patient, requestingPractitioner, requestingOrganization, performingPractitioner);
        final var diagnosticReport = diagnosticReportMapper.mapToDiagnosticReport(
            message, patient, specimens, observations,
            performingPractitioner, performingOrganization, procedureRequest);
        final var messageHeader = messageHeaderMapper.mapToMessageHeader(
            message, diagnosticReport, performingOrganization, requestingOrganization);

        final MedicalReportBuilder medicalReportBuilder = MedicalReport.builder();

        medicalReportBuilder.messageHeader(messageHeader);
        medicalReportBuilder.patient(patient);
        medicalReportBuilder.requestingPractitioner(requestingPractitioner);
        medicalReportBuilder.requestingOrganization(requestingOrganization);
        medicalReportBuilder.performingPractitioner(performingPractitioner);
        medicalReportBuilder.performingOrganization(performingOrganization);
        medicalReportBuilder.testRequestSummary(procedureRequest);
        medicalReportBuilder.testReport(diagnosticReport);
        medicalReportBuilder.specimens(specimens);
        medicalReportBuilder.testResults(observations);

        return medicalReportBuilder.build();
    }
}
