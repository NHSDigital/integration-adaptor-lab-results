package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: INV+MQ+42R4.:911::Serum ferritin'
 */
@AllArgsConstructor
@Getter
@Builder
public class LaboratoryInvestigation extends Segment {

    private static final String KEY = "INV";
    private static final String QUALIFIER = "MQ";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String FIVE_BYTE_READ_CODE = "911";

    private static final int INVESTIGATION_CODE_INDEX = 0;
    private static final int INVESTIGATION_DESCRIPTION_INDEX = 3;

    private final String investigationCode;
    @NonNull
    private final String investigationDescription;

    public static LaboratoryInvestigation fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + LaboratoryInvestigation.class.getSimpleName()
                + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        String investigationCode = Split.byColon(keySplit[2])[INVESTIGATION_CODE_INDEX];
        String investigationDescription = Split.byColon(keySplit[2])[INVESTIGATION_DESCRIPTION_INDEX];

        return new LaboratoryInvestigation(investigationCode, investigationDescription);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        String fiveByteReadCode = !investigationCode.isBlank() ? FIVE_BYTE_READ_CODE : "";

        return QUALIFIER
            + PLUS_SEPARATOR
            + investigationCode
            + COLON_SEPARATOR
            + fiveByteReadCode
            + COLON_SEPARATOR + COLON_SEPARATOR
            + investigationDescription;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (StringUtils.isBlank(investigationDescription)) {
            throw new EdifactValidationException(getKey() + ": Attribute investigationDescription is required");
        }
    }
}
