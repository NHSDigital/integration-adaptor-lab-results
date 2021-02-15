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
class MeshMailBoxSchedulerTest {
    private static final int MAX_WAIT = 5;

    @InjectMocks
    private MeshMailBoxScheduler meshMailBoxScheduler;

    @Mock
    private SchedulerTimestampRepository schedulerTimestampRepository;

    @Mock
    private TimestampService timestampService;

    @Mock
    private ApplicationContext applicationContext;

    @Test
    void when_collectionIsEmpty_expect_singleDocumentIsCreatedAndTheJobIsNotExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong()))
                .thenReturn(false);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(MAX_WAIT);

        assertThat(hasTimePassed).isFalse();
    }

    @Test
    void when_documentExistsAndTimestampIsBeforeProvidedTime_expect_documentIsUpdateAndTheJobIsExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong())).thenReturn(true);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(MAX_WAIT);

        assertThat(hasTimePassed).isTrue();
    }

    @Test
    void when_documentExistsAndTimestampIsAfterProvidedTime_expect_documentIsNotUpdateAndTheJobIsNotExecuted() {
        when(schedulerTimestampRepository.updateTimestamp(anyString(), isA(Instant.class), anyLong()))
                .thenReturn(false);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        boolean hasTimePassed = meshMailBoxScheduler.hasTimePassed(MAX_WAIT);

        assertThat(hasTimePassed).isFalse();
    }

    @Test
    void when_schedulerIsDisabled_expect_returnFalse() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("labresults.scheduler.enabled")).thenReturn("false");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(meshMailBoxScheduler.isEnabled()).isFalse();
    }

    @Test
    void when_schedulerIsEnabled_expect_returnTrue() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("labresults.scheduler.enabled")).thenReturn("true");
        when(applicationContext.getEnvironment()).thenReturn(environment);

        assertThat(meshMailBoxScheduler.isEnabled()).isTrue();
    }
}
