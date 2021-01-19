package uk.nhs.digital.nhsconnect.lab.results;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJms
@EnableScheduling
@SpringBootApplication
public class IntegrationAdapterLabResultsApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegrationAdapterLabResultsApplication.class, args);
	}

}
