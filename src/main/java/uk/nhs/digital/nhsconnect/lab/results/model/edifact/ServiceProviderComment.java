package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example FTX+SPC+++red blood cell seen, Note low platelets'
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class ServiceProviderComment extends Segment {
    private static final String KEY = "FTX";
    private static final String QUALIFIER = "SPC";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String serviceProviderComment;

    public static ServiceProviderComment fromString(String edifactString) {
        if (!edifactString.startsWith(ServiceProviderComment.KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + ServiceProviderComment.class.getSimpleName() + " from " + edifactString);
        }
        String[] split = Split.byPlus(
            Split.bySegmentTerminator(edifactString)[0]
        );
        return new ServiceProviderComment(split[4]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return String.join(PLUS_SEPARATOR,
            QUALIFIER,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            serviceProviderComment);
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (StringUtils.isBlank(serviceProviderComment)) {
            throw new EdifactValidationException(getKey() + ": Attribute freeTextValue is blank or missing");
        }
    }
}
