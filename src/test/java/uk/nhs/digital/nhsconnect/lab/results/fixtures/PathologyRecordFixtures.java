package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import org.hl7.fhir.dstu3.model.Practitioner;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class PathologyRecordFixtures {

    public static PathologyRecord generatePathologyRecord(Practitioner requester) {
        return PathologyRecord.builder()
                .requester(requester)
                .build();
    }
}