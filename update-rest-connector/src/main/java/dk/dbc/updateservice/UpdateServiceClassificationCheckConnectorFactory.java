/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.updateservice;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.client.Client;

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
