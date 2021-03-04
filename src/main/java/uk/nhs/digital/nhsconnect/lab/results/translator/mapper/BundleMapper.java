package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

@Component
@RequiredArgsConstructor
public class BundleMapper {
    private static final List<UriType> BUNDLE_META_PROFILE = List.of(
        new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"));
    private static final String BUNDLE_IDENTIFIER_SYSTEM = "https://tools.ietf.org/html/rfc4122";

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;

    public Bundle mapToBundle(final PathologyRecord pathologyRecord) {
        Bundle bundle = generateInitialPathologyBundle();

        bundle.addEntry()
            .setFullUrl(fullUrlGenerator.generate(pathologyRecord.getRequester()))
            .setResource(pathologyRecord.getRequester());

        Optional.ofNullable(pathologyRecord.getPerformer()).ifPresent(performer ->
            bundle.addEntry()
                .setFullUrl(fullUrlGenerator.generate(performer))
                .setResource(performer)
        );

        bundle.addEntry()
            .setFullUrl(fullUrlGenerator.generate(pathologyRecord.getPatient()))
            .setResource(pathologyRecord.getPatient());

        Optional.ofNullable(pathologyRecord.getTestRequestSummary()).ifPresent(testRequestSummary ->
            bundle.addEntry()
                .setFullUrl(fullUrlGenerator.generate(testRequestSummary))
                .setResource(testRequestSummary)
        );

        pathologyRecord.getSpecimens().forEach(specimen ->
            bundle.addEntry()
                .setFullUrl(fullUrlGenerator.generate(specimen))
                .setResource(specimen));

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
