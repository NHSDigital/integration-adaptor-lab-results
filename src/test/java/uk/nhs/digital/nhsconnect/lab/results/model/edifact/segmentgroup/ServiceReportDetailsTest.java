package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportDateIssued;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ServiceReportDetailsTest {
    private static final LocalDateTime EXPECTED_DATE_TIME =
        LocalDateTime.of(2020, 1, 28, 9, 57);

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
        assertThat(report.getCode())
            .isNotNull()
            .extracting(DiagnosticReportCode::getCode)
            .isEqualTo("N");
    }

    @Test
    void testGetReferenceDiagnosticReport() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "RFF+SRI:13/CH001137K/211010191093",
            "ignore me"
        ));
        var reportReference = assertThat(report.getReference()).isNotNull();

        reportReference
            .extracting(Reference::getNumber)
            .isEqualTo("13/CH001137K/211010191093");
        reportReference
            .extracting(Reference::getTarget)
            .extracting(ReferenceType::getQualifier)
            .isEqualTo("SRI");
    }

    @Test
    void testGetDiagnosticReportStatus() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "STS++UN",
            "ignore me"
        ));
        assertThat(report.getStatus())
            .isNotNull()
            .extracting(DiagnosticReportStatus::getEvent)
            .isEqualTo(ReportStatusCode.UNSPECIFIED);
    }

    @Test
    void testGetDiagnosticReportDateIssued() {
        final var report = new ServiceReportDetails(List.of(
            "ignore me",
            "DTM+ISR:202001280957:203",
            "ignore me"
        ));
        var reportDateIssued = assertThat(report.getDateIssued()).isNotNull();

        reportDateIssued
            .extracting(DiagnosticReportDateIssued::getDateIssued)
            .isEqualTo(EXPECTED_DATE_TIME);
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
        assertThat(report.getSubject())
            .isNotNull()
            .extracting(InvestigationSubject::getEdifactSegments)
            .isEqualTo(List.of("S06", "investigation subject content"));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var report = new ServiceReportDetails(List.of());
        assertAll(
            () -> assertThatThrownBy(report::getCode)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment GIS"),
            () -> assertThatThrownBy(report::getReference)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment RFF+SRI"),
            () -> assertThatThrownBy(report::getDateIssued)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment DTM+ISR"),
            () -> assertThatThrownBy(report::getStatus)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment STS"),
            () -> assertThat(report.getSubject())
                .isNotNull()
                .extracting(InvestigationSubject::getEdifactSegments)
                .isEqualTo(List.of())
        );
    }
}
