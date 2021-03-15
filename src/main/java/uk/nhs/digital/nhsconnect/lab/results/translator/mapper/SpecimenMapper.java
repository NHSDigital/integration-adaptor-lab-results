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
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenQuantity;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.SpecimenDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpecimenMapper {
    private static final String IDENTIFIER_SYSTEM = "http://ehr.acme.org/identifiers/collections";
    private static final String ACCESSION_IDENTIFIER_SYSTEM = "http://lab.acme.org/specimens/2011";
    private static final String SNOMED_CODING_SYSTEM = "http://snomed.info/sct";

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
        final Specimen fhir = new Specimen();
        fhir.setId(uuidGenerator.generateUUID());
        // fhir.identifier = SG16.RFF.C506.1154 (requester)
        edifact.getServiceRequesterReference()
            .map(Reference::getNumber)
            .ifPresent(identifier -> fhir.addIdentifier()
                .setValue(identifier)
                .setSystem(IDENTIFIER_SYSTEM));
        // fhir.accessionIdentifier = SG16.RFF.C506.1154 (provider)
        edifact.getServiceProviderReference()
            .map(Reference::getNumber)
            .ifPresent(identifier -> fhir.setAccessionIdentifier(new Identifier()
                .setValue(identifier)
                .setSystem(ACCESSION_IDENTIFIER_SYSTEM)));
        // fhir.status = [none]
        // fhir.type = SG16.SPC.C832.7866
        Optional.ofNullable(edifact.getCharacteristicType())
            .map(SpecimenCharacteristicType::getTypeOfSpecimen)
            .ifPresent(specimenType -> fhir.getType().addCoding()
                .setDisplay(specimenType)
                .setSystem(SNOMED_CODING_SYSTEM));
        // fhir.receivedTime = SG16.DTM.C507.2380 (SRI)
        edifact.getCollectionReceiptDateTime()
            .map(date -> dateFormatMapper.mapToDate(date.getDateFormat(), date.getCollectionReceiptDateTime()))
            .ifPresent(fhir::setReceivedTime);
        // fhir.collection.collected = SG16.DTM.C507.2380 (SCO)
        edifact.getCollectionDateTime()
            .map(date -> dateFormatMapper.mapToDateTimeType(date.getDateFormat(), date.getCollectionDateTime()))
            .ifPresent(date -> fhir.getCollection().setCollected(date));
        // fhir.collection.quantity.value = SG16.QTY.C186.6060
        edifact.getQuantity().map(SpecimenQuantity::getQuantity)
            .ifPresent(quantity -> fhir.getCollection().getQuantity().setValue(quantity));
        // fhir.collection.quantity.unit = SG16.QTY.C848.6410
        edifact.getQuantity().map(SpecimenQuantity::getQuantityUnitOfMeasure)
            .ifPresent(unit -> fhir.getCollection().getQuantity().setUnit(unit));
        // fhir.collection.bodySite = [none]
        // fhir.collection.extension[fastingStatus] = [none]
        // fhir.note = SG16.FTX.C108.4440(1-5)
        edifact.getFreeTexts().stream()
            .map(FreeTextSegment::getTexts)
            .flatMap(Arrays::stream)
            .map(MappingUtils::unescape)
            .map(text -> new Annotation().setText(text))
            .forEach(fhir::addNote);
        // fhir.collection.collector = [none]

        // and the reference to the patient
        fhir.getSubject().setReference(fullUrlGenerator.generate(patient));

        return fhir;
    }
}
