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
