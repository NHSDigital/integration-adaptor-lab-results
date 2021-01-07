package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler.SchedulerTimestampRepository;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeshMailBoxSchedulerTest {

    @InjectMocks
    private MeshMailBoxScheduler meshMailBoxScheduler;

    @Mock
    private SchedulerTimestampRepository schedulerTimestampRepository;

    @Mock
    private TimestampService timestampService;

    @Mock
    private ApplicationContext applicationContext;

    @Test
    public void when_CollectionIsEmpty_Then_SingleDocumentIsCreatedAndTheJobIsNotExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong())).thenReturn(false);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(5);

        assertThat(hasTimePassed).isFalse();
    }

    @Test
    public void when_DocumentExistsAndTimestampIsBeforeProvidedTime_Then_DocumentIsUpdateAndTheJobIsExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong())).thenReturn(true);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(5);

        assertThat(hasTimePassed).isTrue();
    }

    @Test
    public void when_DocumentExistsAndTimestampIsAfterProvidedTime_Then_DocumentIsNotUpdateAndTheJobIsNotExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong())).thenReturn(false);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(5);

        assertThat(hasTimePassed).isFalse();
    }

    @Test
    void when_SchedulerIsDisabled_Then_ReturnFalse() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("labresults.scheduler.enabled")).thenReturn("false");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(meshMailBoxScheduler.isEnabled()).isFalse();
    }

    @Test
    void when_SchedulerIsEnabled_Then_ReturnTrue() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("labresults.scheduler.enabled")).thenReturn("true");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(meshMailBoxScheduler.isEnabled()).isTrue();
    }
}
