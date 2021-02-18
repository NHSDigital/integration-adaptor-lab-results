package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.SegmentGroup.splitMultipleSegmentGroups;

class SegmentGroupTest {
    @Test
    void testSplitOnEmptyList() {
        final List<List<String>> result = splitMultipleSegmentGroups(List.of(), "key");
        assertThat(result).isEmpty();
    }

    @Test
    void testSplitKeyNotFirst() {
        final List<List<String>> result = splitMultipleSegmentGroups(List.of("not the key"), "key");
        assertThat(result).isEmpty();
    }

    @Test
    void testSplitOnlyKey() {
        final List<List<String>> result = splitMultipleSegmentGroups(List.of("key", "key"), "key");
        assertThat(result)
            .hasSize(2)
            .allMatch(List.of("key")::equals);
    }

    @Test
    void testSplitOnlyOneGroup() {
        final List<List<String>> result = splitMultipleSegmentGroups(List.of("key", "content 1", "content 2"), "key");
        assertThat(result)
            .hasSize(1)
            .contains(List.of("key", "content 1", "content 2"));
    }

    @Test
    void testSplitMultipleGroups() {
        final List<List<String>> result = splitMultipleSegmentGroups(
            List.of("key", "content 1", "key", "content 2"), "key");
        assertThat(result)
            .hasSize(2)
            .contains(
                List.of("key", "content 1"),
                List.of("key", "content 2"));
    }
}
