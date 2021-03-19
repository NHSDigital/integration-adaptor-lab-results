package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.Optional;

/**
 * E.g. {@code ADR++US:FLAT1:12 BROWNBERRIE AVENUE::LEEDS++LS18 5PN'} has no address parts 3 or 5.
 * {@code ADR++++LS18 5PN'} has only a postcode.
 * {@code ADR++US:HIGH TERRACE:LONDON'} has no postcode (NHS002).
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
public class UnstructuredAddress extends Segment {
    public static final String KEY = "ADR";
    private static final String FORMAT = "US";

    private static final int MAX_ADDRESS_LINES = 5;
    private static final int INDEX_ADDRESS = 2;
    private static final int INDEX_POSTCODE = 4;

    private final String format;
    private final String[] addressLines;
    private final String postcode;

    public static UnstructuredAddress fromString(final String edifact) {
        if (!edifact.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + UnstructuredAddress.class.getSimpleName()
                + " from " + edifact);
        }
        final String[] splitByPlus = Split.byPlus(edifact);
        final String[] splitByColon = Split.byColon(splitByPlus[INDEX_ADDRESS]);

        String[] addressLines = null;
        if (splitByColon.length > 1 && splitByColon.length <= MAX_ADDRESS_LINES + 1) {
            addressLines = new String[splitByColon.length - 1];
            System.arraycopy(splitByColon, 1, addressLines, 0, splitByColon.length - 1);
        }

        final String postcode = arrayGetSafe(splitByPlus, INDEX_POSTCODE).orElse(null);
        final String qualifier = edifact.startsWith(KEY + PLUS_SEPARATOR + PLUS_SEPARATOR + FORMAT) ? FORMAT : "";

        return new UnstructuredAddress(qualifier, addressLines, postcode);
    }

    public String[] getAddressLines() {
        return addressLines != null ? addressLines : new String[0];
    }

    public Optional<String> getPostcode() {
        return Optional.ofNullable(postcode);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    private String getAddressValue() {
        if (FORMAT.equals(format)) {
            return format
                + COLON_SEPARATOR
                + String.join(COLON_SEPARATOR, addressLines);
        }
        return "";
    }

    @Override
    public void validate() throws EdifactValidationException {
        // if no postcode, there must be address lines
        if (StringUtils.isBlank(postcode)) {
            if (addressLines == null || addressLines.length <= 1) {
                throw new EdifactValidationException(KEY
                    + ": attribute addressLines is required when postcode is missing");
            }
        }

        // if there are address lines, there must be a format and an address line 1
        if (addressLines != null && addressLines.length > 0) {
            if (!FORMAT.equals(format)) {
                throw new EdifactValidationException(KEY + ": format of '" + FORMAT
                    + "' is required when postCode is missing");
            }
            if (StringUtils.isBlank(addressLines[0])) {
                throw new EdifactValidationException(KEY + ": attribute addressLines[0] is required");
            }
        }
    }
}
