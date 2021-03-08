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
    private final PatientMapper patientMapper;
    private final OrganizationMapper organizationMapper;
    private final SpecimenMapper specimenMapper;
    private final DiagnosticReportMapper diagnosticReportMapper;
    private final ObservationMapper observationMapper;

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        final var patient = patientMapper.mapToPatient(message);
        final var specimens = specimenMapper.mapToSpecimens(message, patient);
        final var performingPractitioner = practitionerMapper.mapToPerformingPractitioner(message);
        final var performingOrganization = organizationMapper.mapToPerformingOrganization(message);
        final var diagnosticReport = diagnosticReportMapper.mapToDiagnosticReport(message,
            patient,
            specimens,
            performingPractitioner.orElse(null),
            performingOrganization.orElse(null));
        pathologyRecordBuilder.patient(patient);
        practitionerMapper.mapToRequestingPractitioner(message)
            .ifPresent(pathologyRecordBuilder::requestingPractitioner);
        organizationMapper.mapToRequestingOrganization(message)
            .ifPresent(pathologyRecordBuilder::requestingOrganization);
        performingPractitioner.ifPresent(pathologyRecordBuilder::performingPractitioner);
        performingOrganization.ifPresent(pathologyRecordBuilder::performingOrganization);
        pathologyRecordBuilder.testReport(diagnosticReport);
        pathologyRecordBuilder.specimens(specimens);
        pathologyRecordBuilder.testResults(observationMapper.mapToTestGroupsAndResults(message));

        return pathologyRecordBuilder.build();
    }
}
