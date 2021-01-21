package uk.nhs.digital.nhsconnect.lab.results.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

public final class ActiveMqContainer extends GenericContainer<ActiveMqContainer> {

    public static final int ACTIVEMQ_PORT = 5672;
    private static ActiveMqContainer container;

    private ActiveMqContainer() {
        super(new ImageFromDockerfile()
            .withFileFromPath("activemq.xml", Path.of("./activemq/activemq.xml"))
            .withFileFromPath("Dockerfile", Path.of("./activemq/Dockerfile"))
        );
        addExposedPort(ACTIVEMQ_PORT);
    }

    public static ActiveMqContainer getInstance() {
        if (container == null) {
            container = new ActiveMqContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        var containerBrokerUri = "amqp://" + getContainerIpAddress() + ":" + getMappedPort(ACTIVEMQ_PORT);
        System.setProperty("LAB_RESULTS_AMQP_BROKERS", containerBrokerUri);
    }
}