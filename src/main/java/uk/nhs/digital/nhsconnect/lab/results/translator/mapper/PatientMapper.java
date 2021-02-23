package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;

import java.util.Optional;

@Component
public class PatientMapper {

    protected static final String SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    public Patient mapToPatient(final Message message) {

        final PatientDetails patientDetails = Optional.ofNullable(message.getServiceReportDetails())
            .map(ServiceReportDetails::getSubject)
            .map(InvestigationSubject::getDetails)
            .orElseThrow(() -> new FhirValidationException("Unable to map message to patient details"));

        final Patient patient = new Patient();

        final PersonName personName = patientDetails.getName();
        Optional.ofNullable(personName).ifPresent(name -> {
            mapIdentifier(name, patient);
            mapName(name, patient);
        });

        mapGender(patientDetails, patient);
        mapDateOfBirth(patientDetails, patient);

        return patient;
    }

    private void mapIdentifier(final PersonName name, final Patient patient) {
        patient.addIdentifier()
            .setValue(name.getNhsNumber())
            .setSystem(SYSTEM);
    }

    private void mapDateOfBirth(final PatientDetails details, final Patient patient) {
        details.getDateOfBirth()
            .map(personDateOfBirth -> new DateType(personDateOfBirth.getDateOfBirth()))
            .ifPresent(patient::setBirthDateElement);
    }

    private void mapGender(final PatientDetails details, final Patient patient) {
        details.getSex().ifPresent(personSex -> {
            final String gender = personSex.getGender().name().toLowerCase();
            patient.setGender(Enumerations.AdministrativeGender.fromCode(gender));
        });
    }

    private void mapName(final PersonName name, final Patient patient) {
        final HumanName humanName = new HumanName();

        Optional.ofNullable(name.getTitle()).ifPresent(humanName::addPrefix);
        Optional.ofNullable(name.getFirstForename()).ifPresent(humanName::addGiven);
        Optional.ofNullable(name.getSecondForename()).ifPresent(humanName::addGiven);
        Optional.ofNullable(name.getSurname()).ifPresent(humanName::setFamily);

        patient.addName(humanName);
    }
}
