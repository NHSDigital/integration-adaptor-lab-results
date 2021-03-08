package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:MagicNumber")
@ExtendWith(MockitoExtension.class)
class ObservationMapperTest {
    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private ObservationMapper mapper;

    @Test
    void testMapToObservationsNonePresent() {
        final Message message = new Message(Collections.emptyList());

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).isEmpty();
    }

    @Test
    void testMapToObservationsMissingRequiredSegments() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N" // LabResult
        ));

        assertThatThrownBy(() -> mapper.mapToObservations(message))
            .isExactlyInstanceOf(MissingSegmentException.class)
            .hasMessageStartingWith("EDIFACT section is missing segment");
    }

    @Test
    void testMapToObservationsOnlyRequiredSegmentLaboratoryInvestigation() {
        when(uuidGenerator.generateUUID()).thenReturn("test-uuid");
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+code:911::description", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference
            "GIS+N", // LabResult
            "INV+MQ+code:911::description", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(2)
            .allSatisfy(observation -> assertThat(observation.getCode().getCoding()).hasSize(1)
                .first()
                .satisfies(coding -> assertAll(
                    () -> assertThat(coding.getCode()).isEqualTo("code"),
                    () -> assertThat(coding.getDisplay()).isEqualTo("description"),
                    () -> assertThat(coding.getSystem()).isEqualTo("http://loinc.org")
                )))
            .allSatisfy(specimen -> assertThat(specimen.getId()).isEqualTo("test-uuid"));
    }

    @Test
    void testMapToObservationsLaboratoryInvestigationMissingCode() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+:911::description", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(1)
            .first()
            .satisfies(observation -> assertThat(observation.getCode().getCoding()).hasSize(1)
                .first()
                .satisfies(coding -> assertAll(
                    () -> assertThat(coding.hasCode()).isFalse(),
                    () -> assertThat(coding.getDisplay()).isEqualTo("description"),
                    () -> assertThat(coding.getSystem()).isEqualTo("http://loinc.org")
                )));
    }

    @Test
    void testMapToTestGroupsAndResultsLaboratoryInvestigationResult() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "RSL+NV+1.23:7++:::units" // LaboratoryInvestigationResult
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getValueQuantity)
            .isNotNull()
            .satisfies(quantity -> assertAll(
                () -> assertThat(quantity.getValue()).isEqualTo("1.23"),
                () -> assertThat(quantity.getComparator()).isEqualTo(QuantityComparator.LESS_THAN),
                () -> assertThat(quantity.getUnit()).isEqualTo("units")
            ));
    }

    @Test
    void testMapToTestGroupsAndResultsTestStatusCode() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "STS++PR" // TestStatus
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getStatus)
            .isEqualTo(ObservationStatus.PRELIMINARY);
    }

    @Test
    void testMapToTestGroupsAndResultsTestStatusCodeUnknown() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getStatus)
            .isEqualTo(ObservationStatus.UNKNOWN);
    }

    @Test
    void testMapToTestGroupsAndResultsFreeTexts() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "FTX+RIT+++multi:line", // FreeTextSegment
            "FTX+RIT+++",
            "FTX+RIT+++comment"
        ));

        final var observations = mapper.mapToObservations(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getComment)
            .isEqualTo("multi line\n\ncomment");
    }

    @Test
    void testMapToTestGroupsAndResultsRange() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "S20+20", // ResultReferenceRange
            "RND+U+-1.0+1.0" // RangeDetail
        ));

        final var observations = mapper.mapToObservations(message);

        final var assertReferenceRange = assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getReferenceRangeFirstRep);
        assertAll(
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getLow)
                .extracting(SimpleQuantity::getValue)
                .extracting(BigDecimal::intValue)
                .isEqualTo(-1),
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getHigh)
                .extracting(SimpleQuantity::getValue)
                .extracting(BigDecimal::intValue)
                .isEqualTo(1)
        );
    }

    @Test
    void testCanMapMultipleTestResults() {
        final Message message = new Message(List.of(
            "S02+02",
            "S06+06",
            "GIS+N",
            "INV+MQ+c:911::d",
            "RFF+ASL:1",
            "GIS+N",
            "INV+MQ+c:911::d",
            "RFF+ASL:1",
            "GIS+N",
            "INV+MQ+c:911::d",
            "RFF+ASL:1"
        ));

        final List<Observation> actual = mapper.mapToObservations(message);

        assertThat(actual).hasSize(3);
    }
}
