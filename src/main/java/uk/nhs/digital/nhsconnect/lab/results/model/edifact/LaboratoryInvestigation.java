package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Optional;

/**
 * Example: INV+MQ+42R4.:911::Serum ferritin'
 */
@AllArgsConstructor
@Builder
public class LaboratoryInvestigation extends Segment {

    private static final String KEY = "INV";
    private static final String QUALIFIER = "MQ";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String FIVE_BYTE_READ_CODE = "911";

    private static final int INVESTIGATION_CODE_INDEX = 0;
    private static final int INVESTIGATION_DESCRIPTION_INDEX = 3;

    private final String investigationCode;

    @Getter
    @NonNull
    private final String investigationDescription;

    public static LaboratoryInvestigation fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + LaboratoryInvestigation.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String investigationCode = Split.byColon(keySplit[2])[INVESTIGATION_CODE_INDEX];
        final String investigationDescription = Split.byColon(keySplit[2])[INVESTIGATION_DESCRIPTION_INDEX];

        return new LaboratoryInvestigation(investigationCode, investigationDescription);
    }

    public Optional<String> getInvestigationCode() {
        return Optional.ofNullable(investigationCode);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(investigationDescription)) {
            throw new EdifactValidationException(KEY + ": Attribute investigationDescription is required");
        }
    }
}
