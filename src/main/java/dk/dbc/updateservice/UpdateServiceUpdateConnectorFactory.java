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
public class UpdateServiceUpdateConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceUpdateConnectorFactory.class);

    public static UpdateServiceUpdateConnector create(String updateServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceUpdateConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceUpdateConnector(client, updateServiceBaseUrl);
    }

    public static UpdateServiceUpdateConnector create(String updateServiceBaseUrl, UpdateServiceUpdateConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceUpdateConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceUpdateConnector(client, updateServiceBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_URL")
    private String updateServiceUrl;

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private UpdateServiceUpdateConnector.TimingLogLevel level;

    UpdateServiceUpdateConnector updateServiceUpdateConnector;

    @PostConstruct
    public void initializeConnector() {
        updateServiceUpdateConnector = UpdateServiceUpdateConnectorFactory.create(updateServiceUrl, level);
    }

    @Produces
    public UpdateServiceUpdateConnector getInstance() {
        return updateServiceUpdateConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        updateServiceUpdateConnector.close();
    }
}
