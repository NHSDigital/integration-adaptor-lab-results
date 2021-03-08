package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
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

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        final var patient = patientMapper.mapToPatient(message);
        final var requestingPractitioner = practitionerMapper.mapToRequestingPractitioner(message);
        final var requestingOrganization = organizationMapper.mapToRequestingOrganization(message);
        final var performingPractitioner = practitionerMapper.mapToPerformingPractitioner(message);
        final var performingOrganization = organizationMapper.mapToPerformingOrganization(message);

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

        return pathologyRecordBuilder.build();
    }
}
