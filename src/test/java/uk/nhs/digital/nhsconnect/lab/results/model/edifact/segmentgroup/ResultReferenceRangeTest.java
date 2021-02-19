package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RangeDetail;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ResultReferenceRangeTest {
    @Test
    void testIndicator() {
        assertThat(ResultReferenceRange.INDICATOR).isEqualTo("S20");
    }

    @Test
    void testGetRangeDetail() {
        final var range = new ResultReferenceRange(List.of(
            "ignore me",
            "RND+U+170+1100",
            "ignore me"
        ));
        assertThat(range.getRangeDetail())
            .isNotNull()
            .extracting(RangeDetail::getValue)
            .isEqualTo("U+170+1100");
    }

    @Test
    void testGetReferencePopulationDefinitionFreeText() {
        final var range = new ResultReferenceRange(List.of(
            "ignore me",
            "FTX+RPD+++Equivocal",
            "ignore me"
        ));
        assertThat(range.getFreeTexts())
            .isPresent()
            .map(FreeTextSegment::getValue)
            .contains("RPD+++Equivocal");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var range = new ResultReferenceRange(List.of());
        assertAll(
            () -> assertThatThrownBy(range::getRangeDetail)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment RND+U"),
            () -> assertThat(range.getFreeTexts()).isEmpty()
        );
    }
}
