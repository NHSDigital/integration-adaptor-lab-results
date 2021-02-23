package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SegmentGroup extends Section {
    public SegmentGroup(final List<String> edifactSegments) {
        super(edifactSegments);
    }

    protected static List<List<String>> splitMultipleSegmentGroups(@NonNull final List<String> edifactSegments,
                                                                   @NonNull final String key) {
        if (edifactSegments.isEmpty() || !edifactSegments.get(0).startsWith(key)) {
            return Collections.emptyList();
        }

        final List<List<String>> results = new ArrayList<>();

        List<String> singleGroup = new ArrayList<>();
        for (final String segment : edifactSegments) {
            if (segment.startsWith(key)) {
                if (!singleGroup.isEmpty()) {
                    results.add(singleGroup);
                }
                singleGroup = new ArrayList<>();
            }
            singleGroup.add(segment);
        }
        if (!singleGroup.isEmpty()) {
            results.add(singleGroup);
        }
        return results;
    }
}
