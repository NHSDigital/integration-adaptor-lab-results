package uk.nhs.digital.nhsconnect.lab.results.outbound.queue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.outbound.fhir.FhirParser;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObjectSerializer {
    private final FhirParser fhirParser;

    @SneakyThrows
    public String serialize(Object object) {
        if (object instanceof IBaseResource) {
            return fhirParser.encodeToString((IBaseResource) object);
        }
        throw new UnsupportedOperationException("Data type " + object.getClass().getSimpleName() + " is not supported");
    }
}
