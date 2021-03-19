package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PatientMapper {

    protected static final String NHS_NUMBER_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private final UUIDGenerator uuidGenerator;

    public Patient mapToPatient(final Message message) {

        final Optional<InvestigationSubject> investigationSubject =
            ofNullable(message.getServiceReportDetails())
                .map(ServiceReportDetails::getSubject);

        final PatientDetails patientDetails = investigationSubject
            .map(InvestigationSubject::getDetails)
            .orElseThrow(() -> new FhirValidationException("Unable to map message to patient details"));

        final Optional<Reference> referenceServiceSubject = investigationSubject
            .flatMap(InvestigationSubject::getReferenceServiceSubject);

        final Patient patient = new Patient();
        patient.setId(uuidGenerator.generateUUID());

        final PersonName personName = patientDetails.getName();
        mapName(personName, patient);

        ofNullable(personName.getNhsNumber())
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
        unstructuredAddress.getPostCode().ifPresent(address::setPostalCode);
        patient.addAddress(address);
    }

    private void mapNhsNumberIdentifier(final String nhsNumber, final Patient patient) {
        final Identifier identifier = new Identifier();
        identifier.setSystem(NHS_NUMBER_SYSTEM);
        identifier.setValue(nhsNumber);
        patient.addIdentifier(identifier);
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

        ofNullable(name.getTitle()).ifPresent(humanName::addPrefix);
        ofNullable(name.getFirstForename()).ifPresent(forename -> {
            humanName.addGiven(forename);
            ofNullable(name.getSecondForename())
                .ifPresent(humanName::addGiven);
        });
        humanName.setFamily(name.getSurname());

        patient.addName(humanName);
    }
}
