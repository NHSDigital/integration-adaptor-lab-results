package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class Segment {
    public static final String PLUS_SEPARATOR = "+";
    protected static final String COLON_SEPARATOR = ":";

    protected static <T> List<T> removeEmptyTrailingFields(List<T> list, Predicate<T> predicate) {
        var result = new ArrayList<T>();
        Collections.reverse(list);
        boolean foundFirstValid = false;
        for (T element : list) {
            if (foundFirstValid || predicate.test(element)) {
                foundFirstValid = true;
                result.add(element);
            }
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * @return the key of the segment for example NAD, DTM
     */
    public abstract String getKey();

    public void validate() throws EdifactValidationException {
    }
}
