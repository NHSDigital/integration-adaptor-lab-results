package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.UriType;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class FhirFixtures {

    private static final int YEAR = 2010;
    private static final int DAY_OF_MONTH = 24;
    private static final int HOUR_OF_DAY = 15;
    private static final int MINUTE = 41;

    public static Patient generatePatient(final String name, final AdministrativeGender gender,
                                          final String birthDate) {
        Patient patient = new Patient();

        patient.setId("some-entry-uuid");
        patient.addName().setText(name);
        patient.setGender(gender);
        patient.setBirthDateElement(new DateType(birthDate));

        return patient;
    }

    public static Practitioner generatePractitioner(final String name, final AdministrativeGender gender) {
        Practitioner requester = new Practitioner();

        requester.setId("some-entry-uuid");
        requester.addName().setText(name);
        requester.setGender(gender);

        return requester;
    }

    public static Organization generateOrganization(final String name) {
        Organization organization = new Organization();

        organization.setId("some-entry-uuid");
        organization.setName(name);

        return organization;
    }

    public static Bundle generateBundle(final PathologyRecord pathologyRecord) {
        Bundle bundle = new Bundle();

        bundle.setMeta(new Meta()
            .setLastUpdatedElement(new InstantType(
                new GregorianCalendar(YEAR, Calendar.FEBRUARY, DAY_OF_MONTH, HOUR_OF_DAY, MINUTE)
            ))
            .setProfile(List.of(new UriType("https://some-profile")))
        );
        bundle.setIdentifier(new Identifier()
            .setSystem("https://some-system")
            .setValue("some-value-uuid")
        );
        bundle.setType(Bundle.BundleType.MESSAGE);

        bundle.addEntry()
            .setFullUrl("urn:uuid:some-entry-uuid")
            .setResource(pathologyRecord.getPatient());

        bundle.addEntry()
            .setFullUrl("urn:uuid:some-entry-uuid")
            .setResource(pathologyRecord.getRequester());

        bundle.addEntry()
            .setFullUrl("urn:uuid:some-entry-uuid")
            .setResource(pathologyRecord.getRequestingOrganization());

        bundle.addEntry()
            .setFullUrl("urn:uuid:some-entry-uuid")
            .setResource(pathologyRecord.getPerformer());

        bundle.addEntry()
            .setFullUrl("urn:uuid:some-entry-uuid")
            .setResource(pathologyRecord.getPerformingOrganization());

        return bundle;
    }
}
