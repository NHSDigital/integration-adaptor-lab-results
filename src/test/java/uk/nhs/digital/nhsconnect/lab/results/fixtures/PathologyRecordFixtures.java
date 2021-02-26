package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;

import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class PathologyRecordFixtures {

    public static PathologyRecord generatePathologyRecord(Patient patient, Practitioner requester,
                                                          Organization requestingOrganization,
                                                          Practitioner performer,
                                                          Organization performingOrganization) {
        return PathologyRecord.builder()
            .patient(patient)
            .requester(requester)
            .requestingOrganization(requestingOrganization)
            .performer(performer)
            .performingOrganization(performingOrganization)
            .build();
    }
}
