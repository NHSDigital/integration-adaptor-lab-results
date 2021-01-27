package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example PDI+2'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Data
public class PersonSex extends Segment {
    protected static final String KEY = "PDI";

    private Gender gender;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return gender.getCode();
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (gender == null) {
            throw new EdifactValidationException(getKey() + ": Gender code is required");
        }
    }

    public static PersonSex fromString(final String edifactString) {
        if (!edifactString.startsWith(PersonSex.KEY)) {
            throw new IllegalArgumentException("Can't create " + PersonSex.class.getSimpleName() + " from " + edifactString);
        }
        final String[] components = Split.byPlus(Split.bySegmentTerminator(edifactString)[0]);
        return PersonSex.builder()
            .gender(Gender.fromCode(components[1]))
            .build();
    }

}
