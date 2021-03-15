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
 * Details of the laboratory investigation.
 * <pre>
 * Example in a Pathology (NHS003) message: {@code INV+MQ+42R4.:911::Serum ferritin'}
 * </pre>
 * <pre>
 * Example in a Screening (NHS004) message: {@code INV+MQ+368481000000103:921::BCS?:FOB result'}
 * </pre>
 */
@AllArgsConstructor
@Builder
public class LaboratoryInvestigation extends Segment {

    private static final String KEY = "INV";
    private static final String QUALIFIER = "MQ";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String investigationCode;
    @Getter
    private final CodingType investigationCodeType;
    @Getter
    @NonNull
    private final String investigationDescription;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static LaboratoryInvestigation fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + LaboratoryInvestigation.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String investigationCode = Split.byColon(keySplit[2])[0];
        final CodingType investigationCodeType = StringUtils.isNotBlank(Split.byColon(keySplit[2])[1])
            ? CodingType.fromCode(Split.byColon(keySplit[2])[1]) : null;
        final String investigationDescription = Split.byColon(keySplit[2])[3];

        return LaboratoryInvestigation.builder()
            .investigationCode(investigationCode)
            .investigationCodeType(investigationCodeType)
            .investigationDescription(investigationDescription)
            .build();
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
