package uk.nhs.digital.nhsconnect.lab.results.configuration;

import com.google.common.base.Strings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "labresults.mongodb")
@Getter
@Setter
@Slf4j
@DependsOn({"appInitializer"}) // so that custom TrustStore is loaded before DB connection is initialized
public class LabResultsMongoClientConfiguration extends AbstractMongoClientConfiguration {

    private String database;

    private String uri;

    private String host;

    private String port;

    private String username;

    private String password;

    private String options;

    private boolean autoIndexCreation;

    private Duration ttl;

    @Override
    public String getDatabaseName() {
        return this.database;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        LOGGER.info("Configuring mongo client settings...");
        builder.applyConnectionString(new ConnectionString(this.createConnectionString()));
    }

    @Override
    protected boolean autoIndexCreation() {
        LOGGER.info("Auto index creation is '{}'", this.autoIndexCreation);
        return this.autoIndexCreation;
    }

    private String createConnectionString() {
        LOGGER.info("Creating a connection string for mongo client settings...");
        if (!Strings.isNullOrEmpty(host)) {
            LOGGER.info("A value was provided from mongodb host. "
                + "Generating a connection string from individual properties.");
            return createConnectionStringFromProperties();
        } else if (!Strings.isNullOrEmpty(uri)) {
            LOGGER.info("A mongodb connection string provided in spring.data.mongodb.uri "
                + "and will be used to configure the database connection.");
            return uri;
        } else {
            LOGGER.error("Mongodb must be configured using a connection string or individual properties. "
                + "Both uri and host are null or empty");
            throw new RuntimeException("Missing mongodb connection string and/or properties");
        }
    }

    private String createConnectionStringFromProperties() {
        String cs = "mongodb://";
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            LOGGER.debug("Including a username and password in the mongo connection string");
            cs += username + ":" + password + "@";
        } else {
            LOGGER.info("No mongodb username or password is configured. Will use an anonymous connection.");
        }
        LOGGER.debug("The generated connection string will used host '{}' and port '{}'", host, port);
        cs += host + ":" + port;
        if (!Strings.isNullOrEmpty(options)) {
            LOGGER.debug("The generated connection will use use options '{}'", options);
            cs += "/?" + options;
        } else {
            LOGGER.warn("No options for the mongodb connection string were provided. "
                + "If connecting to a cluster the driver may not work as expected.");
        }
        return cs;
    }
}
