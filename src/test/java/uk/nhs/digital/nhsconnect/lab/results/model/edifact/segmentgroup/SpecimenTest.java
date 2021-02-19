package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicFastingStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionReceiptDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenQuantity;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenTest {
    @Test
    void testIndicator() {
        assertThat(Specimen.INDICATOR).isEqualTo("S16");
    }

    @Test
    void testGetSequenceDetails() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SEQ++123ABC",
            "ignore me"
        ));
        assertThat(specimen.getSequenceDetails())
            .isPresent()
            .map(SequenceDetails::getValue)
            .contains("123ABC");
    }

    @Test
    void testGetSpecimenCharacteristicType() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SPC+TSP+:::BLOOD & URINE",
            "ignore me"
        ));
        assertThat(specimen.getCharacteristicType())
            .isNotNull()
            .extracting(SpecimenCharacteristicType::getValue)
            .isEqualTo("TSP+:::BLOOD & URINE");
    }

    @Test
    void testGetSpecimenCharacteristicFastingStatus() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SPC+FS+F",
            "ignore me"
        ));
        assertThat(specimen.getCharacteristicFastingStatus())
            .isPresent()
            .map(SpecimenCharacteristicFastingStatus::getValue)
            .contains("FS+F");
    }

    @Test
    void testGetSpecimenReferenceByServiceRequester() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "RFF+RTI:CH000064LX",
            "ignore me"
        ));
        assertThat(specimen.getServiceRequesterReference())
            .isPresent()
            .map(Reference::getValue)
            .contains("RTI:CH000064LX");
    }

    @Test
    void testGetSpecimenReferenceByServiceProvider() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "RFF+STI:CH000064LX",
            "ignore me"
        ));
        assertThat(specimen.getServiceProviderReference())
            .isPresent()
            .map(Reference::getValue)
            .contains("STI:CH000064LX");
    }

    @Test
    void testGetSpecimenQuantity() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "QTY+SVO:1750+:::mL",
            "ignore me"
        ));
        assertThat(specimen.getQuantity())
            .isPresent()
            .map(SpecimenQuantity::getValue)
            .contains("SVO:1750+:::mL");
    }

    @Test
    void testGetSpecimenCollectionDateTime() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "DTM+SCO:20100223:102",
            "ignore me"
        ));
        assertThat(specimen.getCollectionDateTime())
            .isPresent()
            .map(SpecimenCollectionDateTime::getValue)
            .contains("SCO:20100223:102");
    }

    @Test
    void testGetSpecimenCollectionReceiptDateTime() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "DTM+SRI:201002241541:203",
            "ignore me"
        ));
        assertThat(specimen.getCollectionReceiptDateTime())
            .isPresent()
            .map(SpecimenCollectionReceiptDateTime::getValue)
            .contains("SRI:201002241541:203");
    }

    @Test
    void testGetServiceProviderCommentFreeTexts() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "FTX+SPC+++red blood cell seen",
            "ignore me",
            "FTX+SPC+++Note low platelets",
            "ignore me"
        ));
        assertThat(specimen.getFreeTexts())
            .hasSize(2)
            .map(FreeTextSegment::getValue)
            .contains("SPC+++red blood cell seen", "SPC+++Note low platelets");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var specimen = new Specimen(List.of());
        assertAll(
            () -> assertThat(specimen.getSequenceDetails()).isEmpty(),
            () -> assertThatThrownBy(specimen::getCharacteristicType)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment SPC+TSP"),
            () -> assertThat(specimen.getCharacteristicFastingStatus()).isEmpty(),
            () -> assertThat(specimen.getServiceRequesterReference()).isEmpty(),
            () -> assertThat(specimen.getServiceProviderReference()).isEmpty(),
            () -> assertThat(specimen.getQuantity()).isEmpty(),
            () -> assertThat(specimen.getCollectionDateTime()).isEmpty(),
            () -> assertThat(specimen.getCollectionReceiptDateTime()).isEmpty(),
            () -> assertThat(specimen.getFreeTexts()).isEmpty()
        );
    }
}
