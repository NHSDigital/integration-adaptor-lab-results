package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Objects;

/**
 * Example HEA+DM+Y:ZZZ'
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DrugsMarker extends Segment {
    private final static String KEY = "HEA";
    private final static String APT_PREFIX = "DM";
    private final static String ZZZ_SUFFIX = ":ZZZ";
    public final static String KEY_PREFIX = KEY + PLUS_SEPARATOR + APT_PREFIX;

    @Getter
    private final boolean drugsMarker;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return APT_PREFIX
            .concat(PLUS_SEPARATOR)
            .concat(parseDrugMarker())
            .concat(ZZZ_SUFFIX);
    }

    private String parseDrugMarker() {
        if (drugsMarker) {
            return "Y";
        }
        return "%";
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
    }

    @Override
    public void preValidate() throws EdifactValidationException {
    }

    public static DrugsMarker fromString(String edifactString) {
        if (edifactString == null || !edifactString.startsWith(DrugsMarker.KEY_PREFIX)) {
            throw new IllegalArgumentException("Can't create " + DrugsMarker.class.getSimpleName() + " from " + edifactString);
        }
        String[] plusSplit = Split.byPlus(edifactString);
        String marker = Split.byColon(plusSplit[2])[0];
        if (Objects.equals(marker, "Y")) {
            return new DrugsMarker(true);
        } else if (Objects.equals(marker, "%")) {
            return new DrugsMarker(false);
        } else {
            throw new EdifactValidationException("Value: '" + marker + "' of drug marker provided in edifact is not permitted");
        }
    }

}
