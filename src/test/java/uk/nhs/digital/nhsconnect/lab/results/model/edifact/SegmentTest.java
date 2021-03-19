package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SegmentTest {

    @Test
    void testRemoveEmptyTrailingFields() {

        final List<String> segmentValues = Arrays.asList("str1", "str2", "", " ", null);
        final List<String> expected = Arrays.asList("str1", "str2");

        final List<String> actual = Segment.removeEmptyTrailingFields(segmentValues, StringUtils::isNotBlank);

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testArrayGetSafeWithinRangeNonBlank() {
        String[] array = new String[]{"entry"};

        final Optional<String> result = Segment.arrayGetSafe(array, 0);

        assertThat(result).contains("entry");
    }

    @Test
    void testArrayGetSafeWithinRangeBlank() {
        String[] array = new String[]{"", "\t", "\n"};

        final Optional<String> result = Segment.arrayGetSafe(array, 1);

        assertThat(result).isEmpty();
    }

    @Test
    void testArrayGetSafeOutOfRange() {
        String[] array = new String[]{"only one"};

        final Optional<String> result = Segment.arrayGetSafe(array, 2);

        assertThat(result).isEmpty();
    }
}
