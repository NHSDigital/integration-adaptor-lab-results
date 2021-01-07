package uk.nhs.digital.nhsconnect.lab.results;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.container.FakeMeshContainer;
import uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler.SchedulerTimestampRepository;

@Slf4j
public class IntegrationTestsExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        FakeMeshContainer.getInstance().start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var applicationContext = SpringExtension.getApplicationContext(context);

        var schedulerTimestampRepository = applicationContext.getBean(SchedulerTimestampRepository.class);
        schedulerTimestampRepository.deleteAll();
    }
}
