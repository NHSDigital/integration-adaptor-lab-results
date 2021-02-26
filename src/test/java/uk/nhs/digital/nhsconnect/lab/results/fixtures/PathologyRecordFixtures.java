package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;

import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class PathologyRecordFixtures {

    public static PathologyRecord generatePathologyRecord(Practitioner requester, Practitioner performer,
                                                        Patient patient) {
        return PathologyRecord.builder()
            .requester(requester)
            .performer(performer)
            .patient(patient)
            .build();
    }
}
