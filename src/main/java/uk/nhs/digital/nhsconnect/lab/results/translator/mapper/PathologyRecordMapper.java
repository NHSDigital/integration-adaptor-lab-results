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
    private final PatientMapper patientMapper;
    private final SpecimenMapper specimenMapper;

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        practitionerMapper.mapRequester(message).ifPresent(pathologyRecordBuilder::requester);
        practitionerMapper.mapPerformer(message).ifPresent(pathologyRecordBuilder::performer);
        final var patient = patientMapper.mapToPatient(message);
        pathologyRecordBuilder.patient(patient);
        pathologyRecordBuilder.specimens(specimenMapper.mapToSpecimens(message, patient));

        return pathologyRecordBuilder.build();
    }
}
