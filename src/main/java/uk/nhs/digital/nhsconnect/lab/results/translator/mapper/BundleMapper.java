package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BundleMapper {
    private static final List<UriType> BUNDLE_META_PROFILE = List.of(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"));
    private static final String BUNDLE_IDENTIFIER_SYSTEM = "https://tools.ietf.org/html/rfc4122";
    private static final String FULL_URL_PREFIX = "urn:uuid:";

    private final UUIDGenerator uuidGenerator;

    public Bundle mapToBundle(final PathologyRecord pathologyRecord) {
        Bundle bundle = generateInitialPathologyBundle();

        bundle.addEntry()
            .setFullUrl(FULL_URL_PREFIX.concat(uuidGenerator.generateUUID()))
            .setResource(pathologyRecord.getRequester());

        final Patient patient = pathologyRecord.getPatient();
        bundle.addEntry()
            .setFullUrl(FULL_URL_PREFIX.concat(patient.getId()))
            .setResource(patient);

        bundle.addEntry()
            .setFullUrl(FULL_URL_PREFIX.concat(uuidGenerator.generateUUID()))
            .setResource(pathologyRecord.getPatient());

        return bundle;
    }

    private Bundle generateInitialPathologyBundle() {
        Bundle bundle = new Bundle();

        bundle.setMeta(new Meta()
                .setLastUpdatedElement((InstantType.now()))
                .setProfile(BUNDLE_META_PROFILE)
        );
        bundle.setIdentifier(new Identifier()
                .setSystem(BUNDLE_IDENTIFIER_SYSTEM)
                .setValue(uuidGenerator.generateUUID())
        );
        bundle.setType(Bundle.BundleType.MESSAGE);

        return bundle;
    }
}
