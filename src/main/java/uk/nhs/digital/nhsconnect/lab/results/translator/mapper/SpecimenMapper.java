package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenQuantity;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.SpecimenDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_CODING_SYSTEM;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpecimenMapper {
    private static final String IDENTIFIER_SYSTEM = "http://ehr.acme.org/identifiers/collections";
    private static final String ACCESSION_IDENTIFIER_SYSTEM = "http://lab.acme.org/specimens/2011";

    private final UUIDGenerator uuidGenerator;
    private final DateFormatMapper dateFormatMapper;
    private final ResourceFullUrlGenerator fullUrlGenerator;

    public Map<String, Specimen> mapToSpecimensBySequenceNumber(final Message message, final Patient patient) {
        return message.getServiceReportDetails().getSubject().getSpecimens().stream()
            .collect(toMap(
                specimenDetails -> specimenDetails.getSequenceDetails().getNumber(),
                specimenDetails -> edifactToFhir(specimenDetails, patient)));
    }

    private Specimen edifactToFhir(final SpecimenDetails edifact, final Patient patient) {
        final Specimen specimen = new Specimen();
        specimen.getMeta().addProfile(FhirProfiles.SPECIMEN);
        specimen.setId(uuidGenerator.generateUUID());
        // specimen.identifier = SG16.RFF.C506.1154 (requester)
        edifact.getServiceRequesterReference()
            .map(Reference::getNumber)
            .ifPresent(identifier -> specimen.addIdentifier()
                .setValue(identifier)
                .setSystem(IDENTIFIER_SYSTEM));
        // specimen.accessionIdentifier = SG16.RFF.C506.1154 (provider)
        edifact.getServiceProviderReference()
            .map(Reference::getNumber)
            .ifPresent(identifier -> specimen.setAccessionIdentifier(new Identifier()
                .setValue(identifier)
                .setSystem(ACCESSION_IDENTIFIER_SYSTEM)));
        // specimen.status = [none]
        // specimen.type = SG16.SPC.C832
        Optional.ofNullable(edifact.getCharacteristic())
            .ifPresent(specimenCharacteristic -> {
                final var coding = specimen.getType().addCoding()
                    .setSystem(SNOMED_CODING_SYSTEM);
                specimenCharacteristic.getCharacteristic()
                    .ifPresent(coding::setCode);
                specimenCharacteristic.getTypeOfSpecimen()
                    .ifPresent(coding::setDisplay);
            });
        // specimen.receivedTime = SG16.DTM.C507.2380 (SRI)
        edifact.getCollectionReceiptDateTime()
            .map(date -> dateFormatMapper.mapToDate(date.getDateFormat(), date.getCollectionReceiptDateTime()))
            .ifPresent(specimen::setReceivedTime);
        // specimen.collection.collected = SG16.DTM.C507.2380 (SCO)
        edifact.getCollectionDateTime()
            .map(date -> dateFormatMapper.mapToDateTimeType(date.getDateFormat(), date.getCollectionDateTime()))
            .ifPresent(date -> specimen.getCollection().setCollected(date));
        // specimen.collection.quantity.value = SG16.QTY.C186.6060
        edifact.getQuantity().map(SpecimenQuantity::getQuantity)
            .ifPresent(quantity -> specimen.getCollection().getQuantity().setValue(quantity));
        // specimen.collection.quantity.unit = SG16.QTY.C848.6410
        edifact.getQuantity().map(SpecimenQuantity::getQuantityUnitOfMeasure)
            .ifPresent(unit -> specimen.getCollection().getQuantity().setUnit(unit));
        // specimen.collection.bodySite = [none]
        // specimen.collection.extension[fastingStatus] = [none]
        // specimen.note = SG16.FTX.C108.4440(1-5)
        edifact.getFreeTexts().stream()
            .map(FreeTextSegment::getTexts)
            .flatMap(Arrays::stream)
            .map(MappingUtils::unescape)
            .map(text -> new Annotation().setText(text))
            .forEach(specimen::addNote);
        // specimen.collection.collector = [none]

        // and the reference to the patient
        specimen.getSubject().setReference(fullUrlGenerator.generate(patient));

        return specimen;
    }
}
