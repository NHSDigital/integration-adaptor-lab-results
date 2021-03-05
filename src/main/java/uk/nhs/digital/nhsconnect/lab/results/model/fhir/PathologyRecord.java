package uk.nhs.digital.nhsconnect.lab.results.model.fhir;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Specimen;

import java.util.Collections;
import java.util.List;

@Builder
@Getter
@Setter
public class PathologyRecord {

    private Patient patient;
    private Practitioner performingPractitioner;
    private Organization performingOrganization;
    private Practitioner requestingPractitioner;
    private Organization requestingOrganization;
    private Organization specimenCollectingOrganization;
    private Practitioner specimenCollector;
    @Builder.Default
    private List<Specimen> specimens = Collections.emptyList();
    @Builder.Default
    private List<Observation> testGroups = Collections.emptyList();
    private DiagnosticReport testReport;
    private ProcedureRequest testRequestSummary;
    @Builder.Default
    private List<Observation> testResults = Collections.emptyList();
}
