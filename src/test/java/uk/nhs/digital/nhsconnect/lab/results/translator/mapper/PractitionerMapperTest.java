package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PractitionerMapperTest {

    @Mock
    private Message message;

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private PractitionerMapper mapper;

    @Test
    void testMapMessageToRequestingPractitionerWithNoRequester() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToRequestingPractitioner(message)).isEmpty();
    }

    @Test
    void testMapMessageToRequestingPractitionerWithRequester() {
        final var performingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+DEF:900++Alan Turing",
            "SPR+PRO",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingParty));

        Optional<Practitioner> requestingPractitioner = mapper.mapToRequestingPractitioner(message);
        assertThat(requestingPractitioner).isNotEmpty()
            .hasValueSatisfying(practitioner -> assertAll(
                () -> assertThat(practitioner.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Alan Turing"),
                () -> assertThat(practitioner.getIdentifier())
                    .hasSize(1)
                    .first()
                    .satisfies(identifier -> assertAll(
                        () -> assertThat(identifier.getValue()).isEqualTo("DEF"),
                        () -> assertThat(identifier.getSystem()).isEqualTo("https://fhir.nhs.uk/Id/sds-user-id")
                    ))
            ));
    }

    @Test
    void testMapMessageToRequestingPractitionerWithRequesterWithoutIdentifier() {
        final var performingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+++Alan Turing",
            "SPR+PRO",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingParty));

        Optional<Practitioner> requestingPractitioner = mapper.mapToRequestingPractitioner(message);
        assertThat(requestingPractitioner).isNotEmpty()
            .hasValueSatisfying(practitioner -> assertAll(
                () -> assertThat(practitioner.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Alan Turing"),
                () -> assertThat(practitioner.getIdentifier())
                    .hasSize(0)
            ));
    }

    @Test
    void testMapMessageToPerformingPractitionerWithNoPerformer() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToPerformingPractitioner(message)).isEmpty();
    }

    @Test
    void testMapMessageToPerformingPractitionerWithPerformer() {
        final var performingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+ABC:900++Jane Doe",
            "SPR+PRO",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingParty));

        Optional<Practitioner> performingPractitioner = mapper.mapToPerformingPractitioner(message);
        assertThat(performingPractitioner).isNotEmpty()
            .hasValueSatisfying(practitioner -> assertAll(
                () -> assertThat(practitioner.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Jane Doe"),
                () -> assertThat(practitioner.getIdentifier())
                    .hasSize(1)
                    .first()
                    .satisfies(identifier -> assertAll(
                        () -> assertThat(identifier.getValue()).isEqualTo("ABC"),
                        () -> assertThat(identifier.getSystem()).isEqualTo("https://fhir.nhs.uk/Id/sds-user-id")
                    ))
            ));
    }

    @Test
    void testMapMessageWherePerformerIsNotPractitioner() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+++Some Org",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(involvedParty));

        Optional<Practitioner> result = mapper.mapToPerformingPractitioner(message);
        assertThat(result).isEmpty();
    }
}
