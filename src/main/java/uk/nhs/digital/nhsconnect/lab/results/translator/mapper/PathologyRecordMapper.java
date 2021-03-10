package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord.PathologyRecordBuilder;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PathologyRecordMapper {

    private final PractitionerMapper practitionerMapper;
    private final ProcedureRequestMapper procedureRequestMapper;
    private final PatientMapper patientMapper;
    private final OrganizationMapper organizationMapper;
    private final SpecimenMapper specimenMapper;
    private final DiagnosticReportMapper diagnosticReportMapper;
    private final ObservationMapper observationMapper;

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final var patient = patientMapper.mapToPatient(message);
        final var requestingPractitioner = practitionerMapper.mapToRequestingPractitioner(message);
        final var requestingOrganization = organizationMapper.mapToRequestingOrganization(message);
        final var performingPractitioner = practitionerMapper.mapToPerformingPractitioner(message);
        final var performingOrganization = organizationMapper.mapToPerformingOrganization(message);

        final var specimens = specimenMapper.mapToSpecimens(message, patient);
        final var observations = observationMapper.mapToTestGroupsAndResults(message);
        final var diagnosticReport = diagnosticReportMapper.mapToDiagnosticReport(message, patient, specimens,
            observations, performingPractitioner.orElse(null), performingOrganization.orElse(null));

        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        pathologyRecordBuilder.patient(patient);
        requestingPractitioner.ifPresent(pathologyRecordBuilder::requestingPractitioner);
        requestingOrganization.ifPresent(pathologyRecordBuilder::requestingOrganization);
        performingPractitioner.ifPresent(pathologyRecordBuilder::performingPractitioner);
        performingOrganization.ifPresent(pathologyRecordBuilder::performingOrganization);
        procedureRequestMapper.mapToProcedureRequest(message, patient, requestingPractitioner.orElse(null),
            requestingOrganization.orElse(null), performingPractitioner.orElse(null),
            performingOrganization.orElse(null))
            .ifPresent(pathologyRecordBuilder::testRequestSummary);
        pathologyRecordBuilder.specimens(specimenMapper.mapToSpecimens(message, patient));
        pathologyRecordBuilder.testResults(observationMapper.mapToTestGroupsAndResults(message));
        requestingPractitioner.ifPresent(pathologyRecordBuilder::requestingPractitioner);
        requestingOrganization.ifPresent(pathologyRecordBuilder::requestingOrganization);
        performingPractitioner.ifPresent(pathologyRecordBuilder::performingPractitioner);
        performingOrganization.ifPresent(pathologyRecordBuilder::performingOrganization);
        pathologyRecordBuilder.testReport(diagnosticReport);
        pathologyRecordBuilder.specimens(specimens);
        pathologyRecordBuilder.testResults(observations);

        return pathologyRecordBuilder.build();
    }
}
