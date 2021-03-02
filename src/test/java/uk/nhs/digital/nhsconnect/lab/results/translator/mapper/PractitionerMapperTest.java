package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformerNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

@ExtendWith(MockitoExtension.class)
class PractitionerMapperTest {

    @InjectMocks
    private PractitionerMapper mapper;

    @Mock
    private Message message;

    @Mock
    private RequesterNameAndAddress requester;

    @Mock
    private PerformerNameAndAddress performer;

    @Mock
    private UUIDGenerator uuidGenerator;

    @Test
    void testMapMessageToPractitionerNoRequester() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToRequestingPractitioner(message)).isEmpty();
    }

    @Test
    void testMapMessageToPractitionerWithRequester() {
        final var requestingParty = mock(InvolvedParty.class);
        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));
        when(requestingParty.getRequesterNameAndAddress()).thenReturn(Optional.of(requester));
        when(requester.getPractitionerName()).thenReturn("Alan Turing");
        when(requester.getIdentifier()).thenReturn("Identifier");

        Optional<Practitioner> result = mapper.mapToRequestingPractitioner(message);
        assertThat(result).isNotEmpty();

        Practitioner practitioner = result.get();

        assertAll(
            () -> assertThat(practitioner.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Alan Turing"),
            () -> assertThat(practitioner.getIdentifier())
                    .hasSize(1)
                    .first()
                    .satisfies(identifier -> assertAll(
                        () -> assertThat(identifier.getValue()).isEqualTo("Identifier"),
                        () -> assertThat(identifier.getSystem()).isEqualTo("https://fhir.nhs.uk/Id/sds-user-id")
                    ))
        );
    }

    @Test
    void testMapMessageToPractitionerWithUnnamedRequester() {
        final var requestingParty = mock(InvolvedParty.class);
        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));
        when(requestingParty.getRequesterNameAndAddress()).thenReturn(Optional.of(requester));

        Optional<Practitioner> result = mapper.mapToRequestingPractitioner(message);
        assertThat(result).isNotEmpty();

        Practitioner practitioner = result.get();
        assertThat(practitioner.getName()).isEmpty();
    }

    @Test
    void testMapMessageToPractitionerNoPerformer() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToRequestingPractitioner(message)).isEmpty();
    }

    @Test
    void testMapMessageToPractitionerWithPerformer() {
        final var requestingParty = mock(InvolvedParty.class);
        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));
        when(requestingParty.getPerformerNameAndAddress()).thenReturn(Optional.of(performer));
        when(performer.getPractitionerName()).thenReturn("Jane Doe");
        when(performer.getIdentifier()).thenReturn("Identifier");

        Optional<Practitioner> result = mapper.mapToPerformingPractitioner(message);
        assertThat(result).isNotEmpty();

        Practitioner practitioner = result.get();

        assertAll(
            () -> assertThat(practitioner.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo("Jane Doe"),
            () -> assertThat(practitioner.getIdentifier())
                .hasSize(1)
                .first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo("Identifier"),
                    () -> assertThat(identifier.getSystem()).isEqualTo("https://fhir.nhs.uk/Id/sds-user-id")
                ))
        );

    }

    @Test
    void testMapMessageToPractitionerWithUnnamedPerformer() {
        final var requestingParty = mock(InvolvedParty.class);
        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));
        when(requestingParty.getPerformerNameAndAddress()).thenReturn(Optional.of(performer));
        when(performer.getIdentifier()).thenReturn("Identifier");


        Optional<Practitioner> result = mapper.mapToPerformingPractitioner(message);
        assertThat(result).isNotEmpty();

        Practitioner practitioner = result.get();
        assertThat(practitioner.getName()).isEmpty();
    }

    @Test
    void testMapMessageWherePerformerIsNotPractitioner() {
        final var requestingParty = mock(InvolvedParty.class);
        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));
        when(requestingParty.getPerformerNameAndAddress()).thenReturn(Optional.of(performer));
        when(performer.getIdentifier()).thenReturn(null);

        Optional<Practitioner> result = mapper.mapToPerformingPractitioner(message);
        assertThat(result).isEmpty();
    }
}
