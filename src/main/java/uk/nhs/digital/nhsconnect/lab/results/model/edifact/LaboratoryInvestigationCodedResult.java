package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Details of the coded result of a Laboratory Investigation.
 * <pre>
 * Example: RSL+CV+::375211000000108:921::Bowel cancer screening programme FOB test normal (finding)'
 * </pre>
 */
@AllArgsConstructor
@Builder
@Getter
public class LaboratoryInvestigationCodedResult extends Segment {

    private static final String KEY = "RSL";
    private static final String QUALIFIER = "CV";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    @NonNull
    private final String investigationResultCode;
    @NonNull
    private final CodingType investigationResultCodeType;
    @NonNull
    private final String investigationResultDescription;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static LaboratoryInvestigationCodedResult fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create "
                + LaboratoryInvestigationCodedResult.class.getSimpleName() + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);

        final String code = Split.byColon(keySplit[2])[2];
        final String codeType = Split.byColon(keySplit[2])[3];
        final String description = Split.byColon(keySplit[2])[5];

        return LaboratoryInvestigationCodedResult.builder()
            .investigationResultCode(code)
            .investigationResultCodeType(CodingType.fromCode(codeType))
            .investigationResultDescription(description)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (investigationResultCode == null) {
            throw new EdifactValidationException(KEY + ": Attribute investigationResultCode is required");
        }

        if (investigationResultCodeType == null) {
            throw new EdifactValidationException(KEY + ": Attribute investigationResultCodeType is required");
        }

        if (investigationResultDescription == null) {
            throw new EdifactValidationException(KEY + ": Attribute investigationResultDescription is required");
        }
    }
}
