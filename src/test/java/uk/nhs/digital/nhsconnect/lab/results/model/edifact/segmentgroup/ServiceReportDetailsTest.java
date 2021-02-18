package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportDateIssued;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceDiagnosticReport;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ServiceReportDetailsTest {
    @Test
    void testIndicator() {
        assertThat(ServiceReportDetails.INDICATOR).isEqualTo("S02");
    }

    @Test
    void testGetDiagnosticReportCode() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "GIS+N",
            "ignore me"
        ));
        assertThat(report.getDiagnosticReportCode())
            .isNotNull()
            .extracting(DiagnosticReportCode::getValue)
            .isEqualTo("N");
    }

    @Test
    void testGetReferenceDiagnosticReport() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "RFF+SRI:13/CH001137K/211010191093",
            "ignore me"
        ));
        assertThat(report.getReferenceDiagnosticReport())
            .isNotNull()
            .extracting(ReferenceDiagnosticReport::getValue)
            .isEqualTo("SRI:13/CH001137K/211010191093");
    }

    @Test
    void testGetDiagnosticReportStatus() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "STS++UN",
            "ignore me"
        ));
        assertThat(report.getDiagnosticReportStatus())
            .isNotNull()
            .extracting(DiagnosticReportStatus::getValue)
            .isEqualTo("UN");
    }

    @Test
    void testGetDiagnosticReportDateIssued() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "DTM+ISR:202001280957:203",
            "ignore me"
        ));
        assertThat(report.getDiagnosticReportDateIssued())
            .isNotNull()
            .extracting(DiagnosticReportDateIssued::getValue)
            .isEqualTo("ISR:202001280957:203");
    }

    @Test
    void testGetInvestigationSubject() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "S06",
            "investigation subject content",
            "UNT+18+00000003",
            "ignore me"
        ));
        assertThat(report.getInvestigationSubject())
            .isNotNull()
            .extracting(InvestigationSubject::getEdifactSegments)
            .isEqualTo(List.of("S06", "investigation subject content"));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var report = new ServiceReportDetails(List.of());
        assertAll(
            () -> assertThatThrownBy(report::getDiagnosticReportCode)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment GIS"),
            () -> assertThatThrownBy(report::getReferenceDiagnosticReport)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment RFF+SRI"),
            () -> assertThatThrownBy(report::getDiagnosticReportDateIssued)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment DTM+ISR"),
            () -> assertThatThrownBy(report::getDiagnosticReportStatus)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment STS"),
            () -> assertThat(report.getInvestigationSubject())
                .isNotNull()
                .extracting(InvestigationSubject::getEdifactSegments)
                .isEqualTo(List.of())
        );
    }
}
