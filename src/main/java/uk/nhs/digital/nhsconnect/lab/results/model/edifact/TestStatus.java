package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: STS++CO'
 */
@Getter
@Builder
@RequiredArgsConstructor
public class TestStatus extends Segment {

    public static final String KEY = "STS";

    @NonNull
    private final TestStatusCode testStatusCode;

    public static TestStatus fromString(String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + TestStatus.class.getSimpleName()
                + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        String code = keySplit[2];

        return new TestStatus(TestStatusCode.fromCode(code));
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return testStatusCode.getCode();
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (ObjectUtils.isEmpty(testStatusCode)) {
            throw new EdifactValidationException(getKey() + ": Attribute code in testStatusCode is required");
        }
    }
}
