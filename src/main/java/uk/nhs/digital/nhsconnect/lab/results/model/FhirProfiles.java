package uk.nhs.digital.nhsconnect.lab.results.model;

public final class FhirProfiles {

    private FhirProfiles() {
    }

    public static final String PATIENT = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Patient-1";
    public static final String PRACTITIONER = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Practitioner-1";
    public static final String ORGANIZATION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Organization-1";
    public static final String OBSERVATION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Observation-1";
    public static final String PROCEDURE_REQUEST = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-ProcedureRequest-1";
    public static final String DIAGNOSTIC_REPORT = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-DiagnosticReport-1";
    public static final String SPECIMEN = "https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-Specimen-1";
}
