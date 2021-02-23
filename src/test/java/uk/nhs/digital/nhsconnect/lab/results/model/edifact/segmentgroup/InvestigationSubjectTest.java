package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.UnstructuredAddress;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class InvestigationSubjectTest {
    @Test
    void testIndicator() {
        assertThat(InvestigationSubject.INDICATOR).isEqualTo("S06");
    }

    @Test
    void testGetReferenceServiceSubject() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "RFF+SSI:X88442211",
            "ignore me"
        ));
        var referenceServiceSubject = assertThat(investigationSubject.getReferenceServiceSubject()).isPresent();

        referenceServiceSubject
            .map(Reference::getNumber)
            .hasValue("X88442211");
        referenceServiceSubject
            .map(Reference::getTarget)
            .hasValue(ReferenceType.SERVICE_SUBJECT);
    }

    @Test
    void testGetUnstructuredAddress() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "ADR++US:FLAT1:12 BROWNBERRIE AVENUE::LEEDS:++LS18 5PN",
            "ignore me"
        ));
        var investigationSubjectAddress = assertThat(investigationSubject.getAddress()).isPresent();

        investigationSubjectAddress
            .map(UnstructuredAddress::getAddressLines)
            .contains(new String[] {"FLAT1", "12 BROWNBERRIE AVENUE", "", "LEEDS", ""});

        investigationSubjectAddress
            .map(UnstructuredAddress::getFormat)
            .hasValue("US");
        investigationSubjectAddress
            .map(UnstructuredAddress::getPostCode)
            .hasValue("LS18 5PN");
    }

    @Test
    void testGetPatientDetails() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "S07+07",
            "include me",
            "S16+16",
            "ignore me"
        ));
        assertThat(investigationSubject.getDetails())
            .isNotNull()
            .extracting(PatientDetails::getEdifactSegments)
            .isEqualTo(List.of("S07+07", "include me"));
    }

    @Test
    void testGetPatientClinicalInfo() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "S10+10",
            "include me",
            "S16+16",
            "ignore me"
        ));
        assertThat(investigationSubject.getClinicalInfo())
            .isPresent()
            .map(PatientClinicalInfo::getEdifactSegments)
            .contains(List.of("S10+10", "include me"));
    }

    @Test
    void testGetSpecimens() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "S16+16",
            "specimen #1 contents",
            "S16+16",
            "specimen #2 contents",
            "GIS+N",
            "ignore me"
        ));
        assertAll(
            () -> assertThat(investigationSubject.getSpecimens()).hasSize(2)
                .first()
                .extracting(SpecimenDetails::getEdifactSegments)
                .isEqualTo(List.of("S16+16", "specimen #1 contents")),
            () -> assertThat(investigationSubject.getSpecimens())
                .last()
                .extracting(SpecimenDetails::getEdifactSegments)
                .isEqualTo(List.of("S16+16", "specimen #2 contents"))
        );
    }

    @Test
    void testGetLabResults() {
        final var investigationSubject = new InvestigationSubject(List.of(
            "ignore me",
            "GIS+N",
            "result #1 contents",
            "S20+20",
            "result #1 range contents",
            "GIS+N",
            "result #2 contents",
            "UNT+18+00000003",
            "ignore me"
        ));
        assertAll(
            () -> assertThat(investigationSubject.getLabResults()).hasSize(2)
                .first()
                .extracting(LabResult::getEdifactSegments)
                .isEqualTo(List.of("GIS+N", "result #1 contents", "S20+20", "result #1 range contents")),
            () -> assertThat(investigationSubject.getLabResults())
                .last()
                .extracting(LabResult::getEdifactSegments)
                .isEqualTo(List.of("GIS+N", "result #2 contents"))
        );
    }

    @Test
    void testLazyGettersWhenMissing() {
        final var investigationSubject = new InvestigationSubject(List.of());
        assertAll(
            () -> assertThat(investigationSubject.getReferenceServiceSubject()).isEmpty(),
            () -> assertThat(investigationSubject.getAddress()).isEmpty(),
            () -> assertThat(investigationSubject.getDetails()).isNotNull()
                .extracting(PatientDetails::getEdifactSegments)
                .isEqualTo(List.of()),
            () -> assertThat(investigationSubject.getClinicalInfo()).isEmpty(),
            () -> assertThat(investigationSubject.getSpecimens()).isEmpty(),
            () -> assertThat(investigationSubject.getLabResults()).isEmpty()
        );
    }
}
