package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:MagicNumber")
@ExtendWith(MockitoExtension.class)
class ObservationMapperTest {
    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;

    @Mock
    private Patient mockPatient;

    @Mock
    private Organization mockOrganization;

    @Mock
    private Practitioner mockPractitioner;

    @InjectMocks
    private ObservationMapper mapper;

    @BeforeEach
    void setUp() {
        lenient().when(fullUrlGenerator.generate(mockPatient)).thenReturn("");
        lenient().when(fullUrlGenerator.generate(mockOrganization)).thenReturn("");
        lenient().when(fullUrlGenerator.generate(mockPractitioner)).thenReturn("");
    }

    @Test
    void testMapNonePresent() {
        final var message = new Message(Collections.emptyList());

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).isEmpty();
    }

    @Test
    void testMapMissingRequiredSegments() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N" // LabResult
        ));

        assertThatThrownBy(() -> mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner))
            .isExactlyInstanceOf(MissingSegmentException.class)
            .hasMessageStartingWith("EDIFACT section is missing segment");
    }

    @Test
    void testMapOnlyRequiredSegments() {
        when(uuidGenerator.generateUUID()).thenReturn("test-uuid");
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+code:911::description", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference
            "GIS+N", // LabResult
            "INV+MQ+code:911::description", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(2)
            .allSatisfy(observation -> assertThat(observation.getCode().getCoding()).hasSize(1)
                .first()
                .satisfies(coding -> assertAll(
                    () -> assertThat(coding.getCode()).isEqualTo("code"),
                    () -> assertThat(coding.getDisplay()).isEqualTo("description"),
                    () -> assertThat(coding.getSystem()).isEqualTo("http://read.info/readv2")
                )))
            .allSatisfy(specimen -> assertThat(specimen.getId()).isEqualTo("test-uuid"));
    }

    @Test
    void testMapLaboratoryInvestigationMissingCode() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+:911::description", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .satisfies(observation -> assertThat(observation.getCode().getCoding()).hasSize(1)
                .first()
                .satisfies(coding -> assertAll(
                    () -> assertThat(coding.hasCode()).isFalse(),
                    () -> assertThat(coding.getDisplay()).isEqualTo("description"),
                    () -> assertThat(coding.getSystem()).isEqualTo("http://read.info/readv2")
                )));
    }

    @Test
    void testMapLaboratoryInvestigationNumericalResult() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "RSL+NV+1.23:7++:::units" // LaboratoryInvestigationResult
        ));

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

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
    void testMapLaboratoryInvestigationCodedResult() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "RSL+CV+::111222333:921::Normal test" // LaboratoryInvestigationResult
        ));

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getValueCodeableConcept)
            .extracting(CodeableConcept::getCoding)
            .isNotNull()
            .satisfies(result -> assertAll(
                () -> assertThat(result.get(0).getSystem()).isEqualTo("http://snomed.info/sct"),
                () -> assertThat(result.get(0).getCode()).isEqualTo("111222333"),
                () -> assertThat(result.get(0).getDisplay()).isEqualTo("Normal test")
            ));
    }

    @Test
    void testMapInterpretation() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "RSL+NV+1.23:7++:::units+HI" // LaboratoryInvestigationResult
        ));

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getInterpretation)
            .extracting(CodeableConcept::getCoding)
            .asList()
            .hasSize(1)
            .first()
            .usingRecursiveComparison()
            .isEqualTo(new Coding()
                .setCode("H")
                .setSystem("http://hl7.org/fhir/v2/0078")
                .setDisplay("High")
        );
    }

    @Test
    void testMapTestStatusCode() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "STS++PR" // TestStatus
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getStatus)
            .isEqualTo(ObservationStatus.PRELIMINARY);
    }

    @Test
    void testMapTestStatusCodeUnknown() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getStatus)
            .isEqualTo(ObservationStatus.UNKNOWN);
    }

    @Test
    void testMapFreeTexts() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "FTX+RIT+++multi:line", // FreeTextSegment
            "FTX+RIT+++",
            "FTX+RIT+++comment"
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getComment)
            .isEqualTo("multi line\n\ncomment");
    }

    @Test
    void testMapRange() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "S20+20", // ResultReferenceRange
            "RND+U+-1.0+1.0" // RangeDetail
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

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
    void testMapRangeWithUnits() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1", // Reference

            "S20+20", // ResultReferenceRange
            "RND+U+-1.0+1.0+megatons" // RangeDetail
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        final var assertReferenceRange = assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getReferenceRangeFirstRep);
        assertAll(
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getLow)
                .extracting(SimpleQuantity::getUnit)
                .isEqualTo("megatons"),
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getHigh)
                .extracting(SimpleQuantity::getUnit)
                .isEqualTo("megatons")
        );
    }

    @Test
    void testMapPatientAsSubject() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));
        when(fullUrlGenerator.generate(mockPatient)).thenReturn("url-patient");

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(1).first()
            .extracting(Observation::getSubject)
            .extracting(Reference::getReference)
            .isEqualTo("url-patient");
    }

    @Test
    void testMapPractitionerAsPerformer() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));

        when(fullUrlGenerator.generate(mockPractitioner)).thenReturn("url-practitioner");
        when(fullUrlGenerator.generate(mockOrganization)).thenReturn("url-organization");

        final var observations = mapper.mapToObservations(
            message, mockPatient, emptyMap(), mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(1).first()
            .extracting(Observation::getPerformer)
            .satisfies(performers -> assertAll(
                () -> assertThat(performers).hasSize(2),
                () -> assertThat(performers.get(0).getReference()).isEqualTo("url-organization"),
                () -> assertThat(performers.get(1).getReference()).isEqualTo("url-practitioner")
            ));
    }

    @Test
    void testMapBothOrganizationAndPractitionerAsPerfomers() {
        final var message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation
            "RFF+ASL:1" // Reference
        ));
        when(fullUrlGenerator.generate(mockOrganization)).thenReturn("url-organization");
        when(fullUrlGenerator.generate(mockPractitioner)).thenReturn("url-practitioner");
        when(fullUrlGenerator.generate(mockOrganization)).thenReturn("url-organization");

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(1).first()
            .extracting(Observation::getPerformer)
            .satisfies(performers -> assertThat(performers).hasSize(2)
                .map(Reference::getReference)
                .containsExactly("url-organization", "url-practitioner"));
    }

    @Test
    void testCanMapMultipleTestResults() {
        final var message = new Message(List.of(
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

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        assertThat(observations).hasSize(3);
    }

    @Test
    void testMapSpecimen() {
        final var message = new Message(List.of(
            "S02+02",
            "S06+06",
            "S16+16",
            "SEQ++1",
            "GIS+N",
            "INV+MQ+c:911::group",
            "RFF+ASL:1"
        ));
        final var mockSpecimen = mock(Specimen.class);
        when(mockSpecimen.getId()).thenReturn("uuid-specimen");
        when(fullUrlGenerator.generate("uuid-specimen")).thenReturn("url-specimen");

        final var observations = mapper.mapToObservations(message, mockPatient, Map.of("1", mockSpecimen),
            mockOrganization, mockPractitioner);

        assertThat(observations).hasSize(1)
            .map(Observation::getSpecimen)
            .map(Reference::getReference)
            .first()
            .isEqualTo("url-specimen");
    }

    @Test
    void testMapTestGroupsWithBidirectionalReferences() {
        final var message = new Message(List.of(
            "S02+02",
            "S06+06",

            "GIS+N",
            "INV+MQ+c:911::group",
            "SEQ++1",
            "RFF+ASL:1",

            "GIS+N",
            "INV+MQ+c:911::result",
            "RFF+ARL:1"
        ));

        /* We don't know whether the test group or test result will be processed first, so we don't know which
         * observation is given which UUID. But, we can still check the references by training the fullUrlGenerator
         * with known mappings and then verify the actual ID of one matches the actual reference of the other. */
        final var uuidToUrlMap = Map.of("uuid-1", "url-1", "uuid-2", "url-2");
        when(uuidGenerator.generateUUID()).thenReturn("uuid-1", "uuid-2");
        uuidToUrlMap.forEach((uuid, url) ->
            lenient().when(fullUrlGenerator.generate(uuid)).thenReturn(url));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        final var group = getByCodingDisplay(observations, "group");
        final var result = getByCodingDisplay(observations, "result");

        assertAll(
            () -> assertThat(group.getRelated()).as("the group references the result")
                .hasSize(1).first()
                .satisfies(related -> assertAll(
                    () -> assertThat(related.getType()).isEqualTo(ObservationRelationshipType.HASMEMBER),
                    () -> assertThat(related.getTarget().getReference()).isEqualTo(uuidToUrlMap.get(result.getId()))
                ))
        );
    }

    @Test
    void testMapTestResultsWithNoGroup() {
        final var message = new Message(List.of(
            "S02+02",
            "S06+06",

            "GIS+N",
            "INV+MQ+c:911::result",
            "RFF+ASL:1"
        ));

        final var observations = mapper.mapToObservations(message, mockPatient, emptyMap(), mockOrganization,
            mockPractitioner);

        final var result = getByCodingDisplay(observations, "result");
        assertThat(result.getRelated()).isEmpty();
    }

    private static Observation getByCodingDisplay(final List<Observation> actual, final String display) {
        return actual.stream()
            .filter(ob -> display.equals(ob.getCode().getCoding().get(0).getDisplay()))
            .findAny().orElseThrow();
    }
}
