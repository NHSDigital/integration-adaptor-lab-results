package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.BundleMapper;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.PathologyRecordMapper;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EdifactToFhirService {

    private final PathologyRecordMapper pathologyRecordMapper;
    private final BundleMapper bundleMapper;

    public Bundle convertToFhir(final Message message) {
        PathologyRecord pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        return bundleMapper.mapToBundle(pathologyRecord);
    }
}
