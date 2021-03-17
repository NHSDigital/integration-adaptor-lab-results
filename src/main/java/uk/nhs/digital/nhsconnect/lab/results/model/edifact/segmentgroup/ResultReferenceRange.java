package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.FreeTextType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RangeDetail;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.Segment.PLUS_SEPARATOR;

/**
 * Defines the reference limits of the laboratory investigation result item.
 * <p>
 * Segment group 20: {@code S20-RND-FTX}
 * <ul>
 *     <li>{@code RND} is mandatory. Must be qualified with {@code +U}.</li>
 *     <li>{@code FTX} is optional. Must be qualified with {@code +RPD}, if present.</li>
 * </ul>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 * &gt; {@link LabResult}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ResultReferenceRange extends SegmentGroup {
    public static final String INDICATOR = "S20";

    @Getter(lazy = true)
    private final RangeDetail details = RangeDetail.fromString(extractSegment(RangeDetail.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final Optional<FreeTextSegment> freeTexts =
        extractOptionalSegment(FreeTextSegment.KEY + PLUS_SEPARATOR
            + FreeTextType.REFERENCE_POPULATION_DEFINITION.getQualifier())
            .map(FreeTextSegment::fromString);

    public static List<ResultReferenceRange> createMultiple(@NonNull final List<String> edifactSegments) {
        return splitMultipleSegmentGroups(edifactSegments, INDICATOR).stream()
            .map(ResultReferenceRange::new)
            .collect(toList());
    }

    public ResultReferenceRange(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
