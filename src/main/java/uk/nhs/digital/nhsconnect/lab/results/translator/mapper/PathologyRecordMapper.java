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
    private final SpecimenMapper specimenMapper;

    public PathologyRecord mapToPathologyRecord(final Message message) {
        final PathologyRecordBuilder pathologyRecordBuilder = PathologyRecord.builder();

        practitionerMapper.mapRequester(message).ifPresent(pathologyRecordBuilder::requester);
        pathologyRecordBuilder.patient(patientMapper.mapToPatient(message));
        pathologyRecordBuilder.specimens(specimenMapper.mapToSpecimens(message));

        return pathologyRecordBuilder.build();
    }
}
