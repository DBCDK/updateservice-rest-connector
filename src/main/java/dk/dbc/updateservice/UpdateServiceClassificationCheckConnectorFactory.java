package dk.dbc.updateservice;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;

@ApplicationScoped
public class UpdateServiceClassificationCheckConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceClassificationCheckConnectorFactory.class);

    public static UpdateServiceClassificationCheckConnector create(String updateServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceClassificationCheckConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceClassificationCheckConnector(client, updateServiceBaseUrl);
    }

    public static UpdateServiceClassificationCheckConnector create(String updateServiceBaseUrl, UpdateServiceClassificationCheckConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceClassificationCheckConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceClassificationCheckConnector(client, updateServiceBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_URL")
    private String updateServiceUrl;

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_CLASSIFICATION_CHECK_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private UpdateServiceClassificationCheckConnector.TimingLogLevel level;

    UpdateServiceClassificationCheckConnector updateServiceClassificationCheckConnector;

    @PostConstruct
    public void initializeConnector() {
        updateServiceClassificationCheckConnector = UpdateServiceClassificationCheckConnectorFactory.create(updateServiceUrl, level);
    }

    @Produces
    public UpdateServiceClassificationCheckConnector getInstance() {
        return updateServiceClassificationCheckConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        updateServiceClassificationCheckConnector.close();
    }
}
