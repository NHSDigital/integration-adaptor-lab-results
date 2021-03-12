package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.MeshInboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshApiConnectionException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeshServiceTest {

    @Mock
    private MeshClient meshClient;

    @Mock
    private MeshInboundQueueService meshInboundQueueService;

    @Mock
    private MeshMailBoxScheduler meshMailBoxScheduler;

    @Mock
    private CorrelationIdService correlationIdService;

    private final long scanDelayInSeconds = 2L;
    private final long pollingCycleMaximumDurationInSeconds = 1L;
    private static final String MESSAGE_ID1 = "messageId1";
    private static final String MESSAGE_ID2 = "messageId2";
    private static final String ERROR_MESSAGE_ID = "messageId_error";
    private MeshService meshService;
    private MeshMessage meshMessage1;
    private MeshMessage meshMessage2;

    @BeforeEach
    void setUp() {
        meshMessage1 = new MeshMessage();
        meshMessage1.setMeshMessageId(MESSAGE_ID1).setWorkflowId(WorkflowId.PATHOLOGY);
        meshMessage2 = new MeshMessage();
        meshMessage2.setMeshMessageId(MESSAGE_ID2).setWorkflowId(WorkflowId.PATHOLOGY);

        final long scanIntervalInMilliseconds = 6000L;
        meshService = new MeshService(meshClient,
                meshInboundQueueService,
                meshMailBoxScheduler,
                correlationIdService,
                scanDelayInSeconds,
                scanIntervalInMilliseconds,
                pollingCycleMaximumDurationInSeconds);
    }

    @Test
    void when_intervalHasPassedAndMessagesFound_expect_downloadAndPublishAllMessages() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(MESSAGE_ID1, MESSAGE_ID2));
        when(meshClient.getEdifactMessage(MESSAGE_ID1)).thenReturn(meshMessage1);
        when(meshClient.getEdifactMessage(MESSAGE_ID2)).thenReturn(meshMessage2);

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID1),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient).acknowledgeMessage(MESSAGE_ID1),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID2),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient).acknowledgeMessage(MESSAGE_ID2),
            () -> verify(correlationIdService, times(2)).applyRandomCorrelationId(),
            () -> verify(correlationIdService, times(2)).resetCorrelationId()
        );
    }

    @Test
    void when_intervalHasPassed_durationExceeded_expect_stopDownloading() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(MESSAGE_ID1, MESSAGE_ID2));
        when(meshClient.getEdifactMessage(any())).thenAnswer(invocation -> {
            // ensure first download exceeds duration
            Thread.sleep(TimeUnit.SECONDS.toMillis(pollingCycleMaximumDurationInSeconds + 1));
            return meshMessage1;
        });

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID1),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient).acknowledgeMessage(MESSAGE_ID1),
            // second message not downloaded and published
            () -> verifyNoMoreInteractions(meshClient, meshInboundQueueService),
            () -> verify(correlationIdService).applyRandomCorrelationId(),
            () -> verify(correlationIdService).resetCorrelationId()
        );
    }

    @Test
    void when_intervalHasPassedAndRequestToGetMessageListFails_expect_doNotPublishAndAcknowledgeMessages() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenThrow(new MeshApiConnectionException("error"));

        assertAll(
            () -> assertThatThrownBy(meshService::scanMeshInboxForMessages)
                .isExactlyInstanceOf(MeshApiConnectionException.class),
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient, never()).getEdifactMessage(any()),
            () -> verify(meshInboundQueueService, never()).publish(any()),
            () -> verify(meshClient, never()).acknowledgeMessage(any()),
            () -> verifyNoInteractions(correlationIdService)
        );
    }

    @Test
    void when_intervalPassedAndMeshDownloadFailsWithConnectionException_expect_doNotPublishOrAcknowledgeMessage() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(ERROR_MESSAGE_ID));
        when(meshClient.getEdifactMessage(any())).thenThrow(new MeshApiConnectionException("error"));

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(ERROR_MESSAGE_ID),
            () -> verify(meshInboundQueueService, never()).publish(any()),
            () -> verify(meshClient, never()).acknowledgeMessage(MESSAGE_ID1),

            () -> verify(correlationIdService).applyRandomCorrelationId(),
            () -> verify(correlationIdService).resetCorrelationId()
        );
    }

    @Test
    void when_intervalPassedAndDownloadedMessageHasAnUnsupportedWorkflow_expect_doNotPublishOrAcknowledgeMessage() {
        final String MESSAGE_ID = "messageId_with_unsupported_workflow";
        final MeshMessage meshMessage = new MeshMessage();
        meshMessage.setMeshMessageId(MESSAGE_ID).setWorkflowId(WorkflowId.PATHOLOGY_ACK);

        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(MESSAGE_ID));
        when(meshClient.getEdifactMessage(MESSAGE_ID)).thenReturn(meshMessage);

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID),
            () -> verify(meshInboundQueueService, never()).publish(any()),
            () -> verify(meshClient, never()).acknowledgeMessage(MESSAGE_ID),
            () -> verify(correlationIdService).applyRandomCorrelationId(),
            () -> verify(correlationIdService).resetCorrelationId()
        );
    }

    @Test
    void when_intervalPassedAndMeshDownloadFailsWithWorkflowUnknownException_expect_doNotPublishOrAcknowledgeMessage() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(ERROR_MESSAGE_ID));
        when(meshClient.getEdifactMessage(any())).thenThrow(new MeshWorkflowUnknownException("error", "1"));

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(ERROR_MESSAGE_ID),
            () -> verify(meshInboundQueueService, never()).publish(any()),
            () -> verify(meshClient, never()).acknowledgeMessage(MESSAGE_ID1),
            () -> verify(correlationIdService).applyRandomCorrelationId(),
            () -> verify(correlationIdService).resetCorrelationId()
        );
    }

    @Test
    void when_intervalHasPassedAndRequestToDownloadMeshMessageFails_expect_skipMessageAndDownloadNextOne() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(ERROR_MESSAGE_ID, MESSAGE_ID1));
        when(meshClient.getEdifactMessage(ERROR_MESSAGE_ID)).thenThrow(new MeshApiConnectionException("error"));
        when(meshClient.getEdifactMessage(MESSAGE_ID1)).thenReturn(meshMessage1);

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(ERROR_MESSAGE_ID),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID1),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient).acknowledgeMessage(MESSAGE_ID1),
            () -> verify(correlationIdService, times(2)).applyRandomCorrelationId(),
            () -> verify(correlationIdService, times(2)).resetCorrelationId()
        );
    }

    @Test
    void when_intervalHasPassedAndAcknowledgeMeshMessageFails_expect_skipMessageAndDownloadNextOne() {
        final MeshMessage messageForAckError = new MeshMessage();
        messageForAckError.setMeshMessageId(ERROR_MESSAGE_ID).setWorkflowId(WorkflowId.PATHOLOGY);

        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(ERROR_MESSAGE_ID, MESSAGE_ID1));
        doThrow(new MeshApiConnectionException("error")).when(meshClient).acknowledgeMessage(ERROR_MESSAGE_ID);
        doNothing().when(meshClient).acknowledgeMessage(MESSAGE_ID1);
        when(meshClient.getEdifactMessage(ERROR_MESSAGE_ID)).thenReturn(messageForAckError);
        when(meshClient.getEdifactMessage(MESSAGE_ID1)).thenReturn(meshMessage1);

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(ERROR_MESSAGE_ID),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID1),
            () -> verify(meshInboundQueueService).publish(messageForAckError),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient).acknowledgeMessage(ERROR_MESSAGE_ID),
            () -> verify(meshClient).acknowledgeMessage(MESSAGE_ID1),
            () -> verify(correlationIdService, times(2)).applyRandomCorrelationId(),
            () -> verify(correlationIdService, times(2)).resetCorrelationId()
        );
    }

    @Test
    void when_intervalPassedAndPublishingToQueueFails_expect_doNotAcknowledgeMessage() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of(MESSAGE_ID1));
        when(meshClient.getEdifactMessage(any())).thenReturn(meshMessage1);
        doThrow(new RuntimeException("error")).when(meshInboundQueueService).publish(any());

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient).getEdifactMessage(MESSAGE_ID1),
            () -> verify(meshInboundQueueService).publish(meshMessage1),
            () -> verify(meshClient, never()).acknowledgeMessage(MESSAGE_ID1),
            () -> verify(correlationIdService).applyRandomCorrelationId(),
            () -> verify(correlationIdService).resetCorrelationId()
        );
    }

    @Test
    void when_intervalHasNotPassed_expect_doNothing() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(false);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);

        meshService.scanMeshInboxForMessages();

        verifyNoInteractions(meshClient);
        verifyNoInteractions(meshInboundQueueService);
        verifyNoInteractions(correlationIdService);
    }

    @Test
    void when_intervalHasPassedButNoMessagesFound_expect_doNothing() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        when(meshClient.getInboxMessageIds()).thenReturn(List.of());

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verify(meshClient, never()).getEdifactMessage(MESSAGE_ID1),
            () -> verifyNoInteractions(meshInboundQueueService)
        );
    }

    @Test
    void when_intervalHasPassedButAuthenticationFails_expect_stopProcessing() {
        when(meshMailBoxScheduler.hasTimePassed(scanDelayInSeconds)).thenReturn(true);
        when(meshMailBoxScheduler.isEnabled()).thenReturn(true);
        doThrow(new MeshApiConnectionException("Auth fail", HttpStatus.OK, HttpStatus.INTERNAL_SERVER_ERROR))
                .when(meshClient).authenticate();

        Assertions.assertThatThrownBy(() -> meshService.scanMeshInboxForMessages())
                .isExactlyInstanceOf(MeshApiConnectionException.class);

        assertAll(
            () -> verify(meshClient).authenticate(),
            () -> verifyNoMoreInteractions(meshClient),
            () -> verifyNoInteractions(meshInboundQueueService)
        );
    }

    @Test
    void when_schedulerIsDisabled_expect_doNothing() {
        when(meshMailBoxScheduler.isEnabled()).thenReturn(false);

        meshService.scanMeshInboxForMessages();

        assertAll(
            () -> verify(meshMailBoxScheduler, never()).hasTimePassed(scanDelayInSeconds),
            () -> verify(meshClient, never()).getEdifactMessage(MESSAGE_ID1),
            () -> verifyNoInteractions(meshInboundQueueService)
        );
    }

}
