package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.UnstructuredAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PatientMapper {

    protected static final String NHS_NUMBER_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    private final UUIDGenerator uuidGenerator;

    public Patient mapToPatient(final Message message) {

        final Optional<InvestigationSubject> investigationSubject =
            Optional.ofNullable(message.getServiceReportDetails())
                .map(ServiceReportDetails::getSubject);

        final PatientDetails patientDetails = investigationSubject
            .map(InvestigationSubject::getDetails)
            .orElseThrow(() -> new FhirValidationException("Unable to map message to patient details"));

        final Optional<Reference> referenceServiceSubject = investigationSubject
            .flatMap(InvestigationSubject::getReferenceServiceSubject);

        final Patient patient = new Patient();
        patient.getMeta().addProfile(FhirProfiles.PATIENT);
        patient.setId(uuidGenerator.generateUUID());

        final PersonName personName = patientDetails.getName();
        mapName(personName, patient);

        Optional.ofNullable(personName.getNhsNumber())
            .ifPresentOrElse(nhsNumber -> mapNhsNumberIdentifier(nhsNumber, patient),
                () -> referenceServiceSubject.ifPresent(reference -> mapOtherIdentifier(reference, patient)));

        mapGender(patientDetails, patient);
        mapDateOfBirth(patientDetails, patient);

        investigationSubject.flatMap(InvestigationSubject::getAddress)
            .ifPresent(unstructuredAddress -> mapAddress(unstructuredAddress, patient));

        return patient;
    }

    private void mapAddress(final UnstructuredAddress unstructuredAddress, final Patient patient) {
        final Address address = new Address();
        Arrays.stream(unstructuredAddress.getAddressLines()).forEach(address::addLine);
        unstructuredAddress.getPostcode().ifPresent(address::setPostalCode);
        patient.addAddress(address);
    }

    private void mapNhsNumberIdentifier(final String nhsNumber, final Patient patient) {
        patient.addIdentifier()
            .setSystem(NHS_NUMBER_SYSTEM)
            .setValue(nhsNumber)
            .addExtension()
            .setUrl("https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSNumberVerificationStatus-1")
            .setValue(new CodeableConcept().addCoding(
                new Coding()
                    .setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1")
                    .setCode("01")
                    .setDisplay("Number present and verified")
            ));
    }

    private void mapOtherIdentifier(final Reference reference, final Patient patient) {
        final Identifier identifier = new Identifier();
        identifier.setId(reference.getNumber());
        patient.addIdentifier(identifier);
    }

    private void mapDateOfBirth(final PatientDetails details, final Patient patient) {
        details.getDateOfBirth()
            .map(personDateOfBirth -> new DateType(personDateOfBirth.getDateOfBirth()))
            .ifPresent(patient::setBirthDateElement);
    }

    private void mapGender(final PatientDetails details, final Patient patient) {
        details.getSex()
            .map(sex -> sex.getGender().name().toLowerCase())
            .map(Enumerations.AdministrativeGender::fromCode)
            .ifPresent(patient::setGender);
    }

    private void mapName(final PersonName name, final Patient patient) {
        final HumanName humanName = new HumanName();

        Optional.ofNullable(name.getTitle()).ifPresent(humanName::addPrefix);
        Optional.ofNullable(name.getFirstForename()).ifPresent(forename -> {
            humanName.addGiven(forename);
            Optional.ofNullable(name.getSecondForename())
                .ifPresent(humanName::addGiven);
        });
        humanName.setFamily(name.getSurname());
        humanName.setUse(HumanName.NameUse.OFFICIAL);
        patient.addName(humanName);
    }
}
