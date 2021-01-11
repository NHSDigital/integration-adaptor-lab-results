package uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler;

import com.mongodb.MongoWriteException;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler.SchedulerTimestampRepositoryExtensionsImpl.MESH_TIMESTAMP_COLLECTION_NAME;

@ExtendWith(MockitoExtension.class)
class SchedulerTimestampRepositoryExtensionTest {

    private static final String SCHEDULER_TYPE = "meshTimestamp";
    private static final int SECONDS = 300;

    @InjectMocks
    private SchedulerTimestampRepositoryExtensionsImpl schedulerTimestampRepositoryExtensions;

    @Mock
    private MongoOperations mongoOperations;

    @Mock
    private UpdateResult updateResult;

    @Mock
    private TimestampService timestampService;

    @Test
    void whenCollectionDoesNotExistCreateDocumentAndReturnFalse() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(0L);
        final Instant timestamp = Instant.now();
        when(timestampService.getCurrentTimestamp()).thenReturn(timestamp);

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, timestamp, SECONDS);

        assertThat(updated).isFalse();
        final var expected = new SchedulerTimestamp(SCHEDULER_TYPE, timestamp);
        verify(mongoOperations).save(expected, MESH_TIMESTAMP_COLLECTION_NAME);
    }

    @Test
    void whenUnableToCreateInitialDocument_mongoException_thenReturnFalse() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(0L);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());
        final MongoWriteException exception = mock(MongoWriteException.class);
        when(mongoOperations.save(any(), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenThrow(exception);

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, Instant.now(), SECONDS);

        assertThat(updated).isFalse();
    }

    @Test
    void whenUnableToCreateInitialDocument_springException_thenReturnFalse() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(0L);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());
        final DuplicateKeyException exception = mock(DuplicateKeyException.class);
        when(mongoOperations.save(any(), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenThrow(exception);

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, Instant.now(), SECONDS);

        assertThat(updated).isFalse();
    }

    @Test
    void whenUpdatedThenReturnTrue() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(1L);

        when(mongoOperations.updateFirst(isA(Query.class), isA(UpdateDefinition.class), isA(String.class))).thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, Instant.now(), SECONDS);

        assertThat(updated).isTrue();
    }

    @Test
    void whenUpdated_andMoreThanOneDocumentExistsForSchedulerType_thenStillReturnTrue() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(2L);

        when(mongoOperations.updateFirst(isA(Query.class), isA(UpdateDefinition.class), isA(String.class))).thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, Instant.now(), SECONDS);

        assertThat(updated).isTrue();
    }

    @Test
    void whenNotUpdatedThenReturnFalse() {
        when(mongoOperations.count(any(Query.class), eq(MESH_TIMESTAMP_COLLECTION_NAME))).thenReturn(1L);

        when(mongoOperations.updateFirst(isA(Query.class), isA(UpdateDefinition.class), isA(String.class))).thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(0L);
        when(timestampService.getCurrentTimestamp()).thenReturn(Instant.now());

        final boolean updated = schedulerTimestampRepositoryExtensions.updateTimestamp(SCHEDULER_TYPE, Instant.now(), SECONDS);

        assertThat(updated).isFalse();
    }

}
