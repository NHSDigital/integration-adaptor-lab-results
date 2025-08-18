package uk.nhs.digital.nhsconnect.lab.results.mesh;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.MeshInboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MessageType;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.utils.CorrelationIdService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MeshService {

    private static final Map<WorkflowId, MessageType> SUPPORTED_WORKFLOWS_TO_TYPES = Map.of(
        WorkflowId.PATHOLOGY_2, MessageType.PATHOLOGY_VARIANT_2,
        WorkflowId.PATHOLOGY_3, MessageType.PATHOLOGY_VARIANT_3,
        WorkflowId.SCREENING, MessageType.SCREENING
    );

    private final MeshClient meshClient;

    private final MeshInboundQueueService meshInboundQueueService;

    private final MeshMailBoxScheduler meshMailBoxScheduler;

    private final CorrelationIdService correlationIdService;

    private final long pollingCycleMinimumIntervalInSeconds;

    private final long wakeupIntervalInMilliseconds;

    private final long pollingCycleDurationInSeconds;

    // suppress this warning as it appears to be a false positive for the parameter dependency injection
    // of MeshInboundQueueService.
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public MeshService(
        MeshClient meshClient,
        MeshInboundQueueService meshInboundQueueService,
        MeshMailBoxScheduler meshMailBoxScheduler,
        CorrelationIdService correlationIdService,
        @Value("${labresults.mesh.pollingCycleMinimumIntervalInSeconds}") long pollingCycleMinimumIntervalInSeconds,
        @Value("${labresults.mesh.wakeupIntervalInMilliseconds}") long wakeupIntervalInMilliseconds,
        @Value("${labresults.mesh.pollingCycleDurationInSeconds}") long pollingCycleDurationInSeconds
    ) {
        this.meshClient = meshClient;
        this.meshInboundQueueService = meshInboundQueueService;
        this.meshMailBoxScheduler = meshMailBoxScheduler;
        this.correlationIdService = correlationIdService;
        this.pollingCycleMinimumIntervalInSeconds = pollingCycleMinimumIntervalInSeconds;
        this.wakeupIntervalInMilliseconds = wakeupIntervalInMilliseconds;
        this.pollingCycleDurationInSeconds = pollingCycleDurationInSeconds;
    }

    @Scheduled(fixedRateString = "${labresults.mesh.wakeupIntervalInMilliseconds}")
    public void scanMeshInboxForMessages() {
        if (!meshMailBoxScheduler.isEnabled()) {
            LOGGER.warn("Not running the MESH mailbox polling cycle because it is disabled. Set variable "
                + "LAB_RESULTS_SCHEDULER_ENABLED to true to enable it.");
            return;
        }
        LOGGER.info("Requesting lock from database to run MESH mailbox polling cycle");
        final StopWatch pollingCycleElapsedTime = new StopWatch();
        pollingCycleElapsedTime.start();
        if (meshMailBoxScheduler.hasTimePassed(pollingCycleMinimumIntervalInSeconds)) {
            final List<String> inboxMessageIds = authenticateAndGetInboxMessageIds();
            for (int i = 0; i < inboxMessageIds.size(); i++) {
                final String messageId = inboxMessageIds.get(i);
                if (sufficientTimeRemainsInPollingCycle(pollingCycleElapsedTime)) {
                    processSingleMessage(messageId);
                } else {
                    LOGGER.warn("Insufficient time remains to complete the polling cycle. "
                        + "Processed {} of {} messages from inbox.", i + 1, inboxMessageIds.size());
                    return;
                }
            }
            LOGGER.info("Completed MESH mailbox polling cycle. Processed all messages from inbox.");
        } else {
            LOGGER.info("Could not obtain database lock to run MESH mailbox polling cycle: insufficient time has "
                    + "elapsed since the previous polling cycle or another adaptor instance has already "
                    + "started the polling cycle. Next scan in {} seconds",
                TimeUnit.MILLISECONDS.toSeconds(wakeupIntervalInMilliseconds));
        }
    }

    private List<String> authenticateAndGetInboxMessageIds() {
        LOGGER.info("Starting MESH mailbox polling cycle");
        meshClient.authenticate();
        LOGGER.info("Authenticated with MESH API at start of polling cycle");
        final List<String> inboxMessageIds = meshClient.getInboxMessageIds();
        LOGGER.info("There are {} messages in the MESH mailbox", inboxMessageIds.size());
        return inboxMessageIds;
    }

    private boolean sufficientTimeRemainsInPollingCycle(StopWatch stopWatch) {
        return stopWatch.getTime(TimeUnit.SECONDS) < pollingCycleDurationInSeconds;
    }

    private void processSingleMessage(String messageId) {
        try {
            correlationIdService.applyRandomCorrelationId();
            LOGGER.debug("Downloading MeshMessageId={}", messageId);
            final InboundMeshMessage meshMessage = meshClient.getEdifactMessage(messageId);

            if (isSupportedMessage(meshMessage.getWorkflowId())) {
                LOGGER.debug("Publishing content of MeshMessageId={} to inbound mesh MQ", messageId);
                meshInboundQueueService.publish(meshMessage);
                LOGGER.debug("Acknowledging MeshMessageId={} on MESH API", messageId);
                meshClient.acknowledgeMessage(meshMessage.getMeshMessageId());
                LOGGER.info("Published MeshMessageId={} for inbound processing", meshMessage.getMeshMessageId());
            } else {
                LOGGER.info(
                    "Downloaded MeshMessageId={} has an unsupported MeshWorkflowId={}."
                        + "It is not a {} message, therefore it has been left in the inbox.",
                    meshMessage.getMeshMessageId(), meshMessage.getWorkflowId(),
                    SUPPORTED_WORKFLOWS_TO_TYPES.values().stream().map(MessageType::getCode)
                        .collect(Collectors.joining(" or "))
                );
            }
        } catch (MeshWorkflowUnknownException ex) {
            LOGGER.warn(
                "MeshMessageId={} has an unknown MeshWorkflowId={} and has been left in the inbox.",
                messageId,
                ex.getWorkflowId()
            );
        } catch (Exception ex) {
            LOGGER.error("Error during reading of MESH message. MeshMessageId={}", messageId, ex);
            // skip message with error and attempt to download the next one
        } finally {
            correlationIdService.resetCorrelationId();
        }
    }

    private boolean isSupportedMessage(WorkflowId workflowId) {
        return SUPPORTED_WORKFLOWS_TO_TYPES.containsKey(workflowId);
    }
}
