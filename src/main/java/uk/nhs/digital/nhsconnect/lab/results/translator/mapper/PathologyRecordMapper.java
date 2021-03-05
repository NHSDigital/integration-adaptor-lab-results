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

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        final var patient = patientMapper.mapToPatient(message);
        final var specimens = specimenMapper.mapToSpecimens(message, patient);
        pathologyRecordBuilder.patient(patient);
        practitionerMapper.mapToRequestingPractitioner(message)
            .ifPresent(pathologyRecordBuilder::requestingPractitioner);
        organizationMapper.mapToRequestingOrganization(message)
            .ifPresent(pathologyRecordBuilder::requestingOrganization);
        practitionerMapper.mapToPerformingPractitioner(message)
            .ifPresent(pathologyRecordBuilder::performingPractitioner);
        organizationMapper.mapToPerformingOrganization(message)
            .ifPresent(pathologyRecordBuilder::performingOrganization);
        pathologyRecordBuilder.testReport(diagnosticReportMapper.map(message, patient, specimens));
        pathologyRecordBuilder.specimens(specimens);

        return pathologyRecordBuilder.build();
    }
}
