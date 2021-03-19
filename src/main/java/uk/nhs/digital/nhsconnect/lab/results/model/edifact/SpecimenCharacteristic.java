package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Optional;

/**
 * NHS002 Example: {@code SPC+TSP+T016:920'}
 * <br/>
 * NHS003 Example: {@code SPC+TSP+:::BLOOD & URINE'}
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SpecimenCharacteristic extends Segment {
    private static final String KEY = "SPC";
    private static final String QUALIFIER = "TSP";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private static final int INDEX_DETAILS = 2;
    private static final int INDEX_CHARACTERISTIC = 0;
    private static final int INDEX_TYPE_OF_SPECIMEN = 3;

    private final String characteristic;
    private final String typeOfSpecimen;

    public static SpecimenCharacteristic fromString(final String edifact) {
        if (!edifact.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenCharacteristic.class.getSimpleName() + " from " + edifact);
        }

        final String[] segmentSplit = Split.byPlus(edifact);
        final String[] detailsSplit = Split.byColon(segmentSplit[INDEX_DETAILS]);
        final var characteristic = arrayGetSafe(detailsSplit, INDEX_CHARACTERISTIC).orElse(null);
        final var typeOfSpecimen = arrayGetSafe(detailsSplit, INDEX_TYPE_OF_SPECIMEN).orElse(null);

        return new SpecimenCharacteristic(characteristic, typeOfSpecimen);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public Optional<String> getCharacteristic() {
        return Optional.ofNullable(characteristic);
    }

    public Optional<String> getTypeOfSpecimen() {
        return Optional.ofNullable(typeOfSpecimen);
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(characteristic) && StringUtils.isBlank(typeOfSpecimen)) {
            throw new EdifactValidationException(
                KEY + ": at least one of characteristic and typeOfSpecimen is required");
        }
    }
}

