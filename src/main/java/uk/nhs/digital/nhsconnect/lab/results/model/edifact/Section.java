package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Section {
    @Getter
    private final List<String> edifactSegments;

    public Section(final List<String> edifactSegments) {
        this.edifactSegments = edifactSegments;
    }

    protected List<String> extractSegments(final String key) {
        return edifactSegments.stream()
            .map(String::strip)
            .filter(segment -> segment.startsWith(key))
            .collect(Collectors.toList());
    }

    protected Optional<String> extractOptionalSegment(final String key) {
        return extractSegments(key).stream()
            .findFirst();
    }

    protected String extractSegment(final String key) {
        return extractOptionalSegment(key)
            .orElseThrow(() -> new MissingSegmentException("EDIFACT section is missing segment " + key));
    }

}
