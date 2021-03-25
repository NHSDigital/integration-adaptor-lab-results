package uk.nhs.digital.nhsconnect.lab.results.responder.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.jms.Message;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class Responder {

    public enum Mode {
        SUCCESS, ERROR, RANDOM
    }

    public enum TemplateType {
        SUCCESS, ERROR
    }

    public static final Map<TemplateType, String> TEMPLATES = Map.of(
        TemplateType.SUCCESS,
        asString(new DefaultResourceLoader().getResource("classpath:templates/OperationOutcome.success.json")),
        TemplateType.ERROR,
        asString(new DefaultResourceLoader().getResource("classpath:templates/OperationOutcome.error.json"))
    );

    @Value("${responder.amqp.gpInboundQueueName}")
    private String gpInboundQueueName;

    @Value("${responder.mode}")
    private Mode mode;

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "${responder.amqp.gpOutboundQueueName}")
    public void receive(final Message message) throws Exception {
        var fhirId = message.getStringProperty(JmsHeaders.FHIR_ID);
        if (StringUtils.isBlank(fhirId)) {
            throw new IllegalArgumentException("Incoming message has no " + JmsHeaders.FHIR_ID + " header");
        }
        var correlationId = message.getStringProperty(JmsHeaders.CORRELATION_ID);

        publishResponse(fhirId, correlationId);
    }

    private void publishResponse(@NonNull String fhirId, String correlationId) {
        var templateType = getTemplateType();
        LOGGER.info("Sending response FhirId={} CorrelationId={} Mode={} TemplateType={}", fhirId, correlationId, mode, templateType);
        jmsTemplate.send(gpInboundQueueName, session -> {
            var message = session.createTextMessage(TEMPLATES.get(templateType));
            message.setStringProperty(JmsHeaders.FHIR_ID, fhirId);
            if (StringUtils.isNotBlank(correlationId)) {
                message.setStringProperty(JmsHeaders.CORRELATION_ID, correlationId);
            }
            return message;
        });
    }

    private TemplateType getTemplateType() {
        switch (mode) {
            case SUCCESS:
                return TemplateType.SUCCESS;
            case ERROR:
                return TemplateType.ERROR;
            case RANDOM:
                var options = TemplateType.values();
                int random = (int)(Math.random() * options.length);
                return options[random];
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static String asString(Resource resource) {
        try (var reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
