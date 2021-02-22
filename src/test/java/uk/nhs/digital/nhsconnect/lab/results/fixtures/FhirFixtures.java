package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.UriType;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class FhirFixtures {

    private static final int YEAR = 2010;
    private static final int DAY_OF_MONTH = 24;
    private static final int HOUR_OF_DAY = 15;
    private static final int MINUTE = 41;

    public static Practitioner generatePractitioner(final String name, final AdministrativeGender gender) {
        Practitioner requester = new Practitioner();

        requester.addName().setText(name);
        requester.setGender(gender);

        return requester;
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
                .setResource(pathologyRecord.getRequester());

        bundle.addEntry()
                .setFullUrl("urn:uuid:some-entry-uuid")
                .setResource(pathologyRecord.getPerformer());

        return bundle;
    }
}
