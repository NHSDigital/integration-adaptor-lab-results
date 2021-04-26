package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.UriType;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BundleMapper {
    private static final List<UriType> BUNDLE_META_PROFILE = List.of(
        new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"));
    private static final String BUNDLE_IDENTIFIER_SYSTEM = "https://tools.ietf.org/html/rfc4122";

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;

    public Bundle mapToBundle(final MedicalReport medicalReport) {
        final Bundle bundle = generateInitialPathologyBundle();
        final Consumer<DomainResource> addToBundle = resource -> addResourceToBundle(bundle, resource);

        Stream.of(
            Stream.of(
                medicalReport.getMessageHeader(),
                medicalReport.getPatient(),
                medicalReport.getRequestingPractitioner(),
                medicalReport.getRequestingOrganization(),
                medicalReport.getPerformingPractitioner(),
                medicalReport.getPerformingOrganization(),
                medicalReport.getTestRequestSummary(),
                medicalReport.getTestReport()
            ),
            medicalReport.getSpecimens().stream(),
            medicalReport.getTestResults().stream()
        )
            .reduce(Stream.empty(), Stream::concat)
            .forEach(addToBundle);

        return bundle;
    }

    private Bundle generateInitialPathologyBundle() {
        final Bundle bundle = new Bundle();

        bundle.setId(uuidGenerator.generateUUID());
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

    private void addResourceToBundle(final Bundle bundle, final Resource resource) {
        bundle.addEntry()
            .setFullUrl(fullUrlGenerator.generate(resource))
            .setResource(resource);
    }
}
