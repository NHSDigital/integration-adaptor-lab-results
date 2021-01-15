package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.*;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class ReferenceTransactionType extends Segment {

    public static final String KEY = "RFF";
    public static final String QUALIFIER = "950";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;
    private @NonNull TransactionType transactionType;

    public static ReferenceTransactionType fromString(final String edifactString) {
        if (!edifactString.startsWith(ReferenceTransactionType.KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + ReferenceTransactionType.class.getSimpleName() + " from " + edifactString);
        }
        final String[] split = Split.byColon(edifactString);
        return new ReferenceTransactionType(TransactionType.fromCode(split[1]));
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (transactionType == null) {
            throw new EdifactValidationException(getKey() + ": Attribute transactionType is required");
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + ":" + transactionType.getCode();
    }

    @Override
    protected void validateStateful() {
        // no stateful properties to validate
    }

}
