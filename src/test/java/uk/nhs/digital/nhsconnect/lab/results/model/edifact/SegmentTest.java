package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SegmentTest {

    @Test
    void testRemoveEmptyTrailingFields() {

        final List<String> segmentValues = Arrays.asList("str1", "str2", "", " ", null);
        final List<String> expected = Arrays.asList("str1", "str2");

        final List<String> actual = Segment.removeEmptyTrailingFields(segmentValues, StringUtils::isNotBlank);

        assertEquals(2, actual.size());
        assertEquals(expected, actual);
    }
}
