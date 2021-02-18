package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RangeDetail;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferencePopulationDefinitionFreeText;

/**
 * Defines the reference limits of the laboratory investigation result item.
 * <p>
 * Segment group 20: {@code S20-RND-FTX}
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 * &gt; {@link LabResult}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ResultReferenceRange extends SegmentGroup {
    public static final String INDICATOR = "S20";

    // RND+U
    @Getter(lazy = true)
    private final RangeDetail rangeDetail = RangeDetail.fromString(extractSegment(RangeDetail.KEY_QUALIFIER));

    // FTX?
    @Getter
    private final Optional<ReferencePopulationDefinitionFreeText> referencePopulationDefinitionFreeText =
        extractOptionalSegment(ReferencePopulationDefinitionFreeText.KEY_QUALIFIER)
            .map(ReferencePopulationDefinitionFreeText::fromString);

    public static List<ResultReferenceRange> createMultiple(@NonNull final List<String> edifactSegments) {
        return splitMultipleSegmentGroups(edifactSegments, INDICATOR).stream()
            .map(ResultReferenceRange::new)
            .collect(toList());
    }

    public ResultReferenceRange(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
