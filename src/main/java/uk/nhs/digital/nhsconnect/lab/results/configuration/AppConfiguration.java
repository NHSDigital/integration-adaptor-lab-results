package uk.nhs.digital.nhsconnect.lab.results.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "labresults")
@Getter
@Setter
public class AppConfiguration {

    private String trustStoreUrl;
    private String trustStorePassword;

    @Bean
    public AmazonS3 getS3Client() {
        if (trustStoreUrl != null && trustStoreUrl.startsWith("s3")) {
            return AmazonS3ClientBuilder.standard().build();
        }
        return null;
    }
}

