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
public class UpdateServiceDoubleRecordCheckConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceDoubleRecordCheckConnectorFactory.class);

    public static UpdateServiceDoubleRecordCheckConnector create(String updateServiceBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceDoubleRecordCheckConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceDoubleRecordCheckConnector(client, updateServiceBaseUrl);
    }

    public static UpdateServiceDoubleRecordCheckConnector create(String updateServiceBaseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig()
                .register(new JacksonFeature()));
        LOGGER.info("Creating UpdateServiceDoubleRecordCheckConnector for: {}", updateServiceBaseUrl);
        return new UpdateServiceDoubleRecordCheckConnector(client, updateServiceBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_URL")
    private String updateServiceUrl;

    @Inject
    @ConfigProperty(name = "UPDATE_SERVICE_DOUBLE_RECORD_CHECK_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private UpdateServiceDoubleRecordCheckConnector.TimingLogLevel level;

    UpdateServiceDoubleRecordCheckConnector updateServiceDoubleRecordCheckConnector;

    @PostConstruct
    public void initializeConnector() {
        updateServiceDoubleRecordCheckConnector = UpdateServiceDoubleRecordCheckConnectorFactory.create(updateServiceUrl, level);
    }

    @Produces
    public UpdateServiceDoubleRecordCheckConnector getInstance() {
        return updateServiceDoubleRecordCheckConnector;
    }

    @PreDestroy
    public void tearDownConnector() {
        updateServiceDoubleRecordCheckConnector.close();
    }
}
