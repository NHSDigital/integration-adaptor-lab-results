package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import com.google.common.collect.Lists;
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
    private final ObservationMapper observationMapper;

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final var patient = patientMapper.mapToPatient(message);
        final var requestingPractitioner = practitionerMapper.mapToRequestingPractitioner(message);
        final var requestingOrganization = organizationMapper.mapToRequestingOrganization(message);
        final var performingPractitioner = practitionerMapper.mapToPerformingPractitioner(message);
        final var performingOrganization = organizationMapper.mapToPerformingOrganization(message);
        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, patient,
            requestingPractitioner.orElse(null), requestingOrganization.orElse(null),
            performingPractitioner.orElse(null), performingOrganization.orElse(null));
        final var specimens = specimenMapper.mapToSpecimens(message, patient);
        final var observations = observationMapper.mapToObservations(message, patient, specimens,
            performingOrganization.orElse(null), performingPractitioner.orElse(null));

        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        pathologyRecordBuilder.patient(patient);
        requestingPractitioner.ifPresent(pathologyRecordBuilder::requestingPractitioner);
        requestingOrganization.ifPresent(pathologyRecordBuilder::requestingOrganization);
        performingPractitioner.ifPresent(pathologyRecordBuilder::performingPractitioner);
        performingOrganization.ifPresent(pathologyRecordBuilder::performingOrganization);
        procedureRequest.ifPresent(pathologyRecordBuilder::testRequestSummary);
        pathologyRecordBuilder.specimens(Lists.newArrayList(specimens.values()));
        pathologyRecordBuilder.testResults(observations);

        return pathologyRecordBuilder.build();
    }
}
