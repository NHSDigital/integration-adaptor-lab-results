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

import java.util.List;

@Builder
@Getter
@Setter
public class PathologyRecord {
    private Patient patient;
    private Practitioner performer;
    private Organization performingOrganization;
    private Practitioner requester;
    private Organization requestingOrganization;
    private Organization specimenCollectingOrganization;
    private Practitioner specimenCollector;
    private List<Specimen> specimens;
    private List<Observation> testGroups;
    private DiagnosticReport testReport;
    private ProcedureRequest testRequestSummary;
    private List<Observation> testResults;
}
