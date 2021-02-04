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
public class ServiceProviderCommentFreeText extends Segment {
    private static final String KEY = "FTX";
    private static final String QUALIFIER = "SPC";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int FREE_TEXT_INDEX = 4;

    private final String serviceProviderComment;

    public static ServiceProviderCommentFreeText fromString(String edifactString) {
        if (!edifactString.startsWith(ServiceProviderCommentFreeText.KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + ServiceProviderCommentFreeText.class.getSimpleName() + " from " + edifactString);
        }
        String[] split = Split.byPlus(
            Split.bySegmentTerminator(edifactString)[0]
        );
        return new ServiceProviderCommentFreeText(split[FREE_TEXT_INDEX]);
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
