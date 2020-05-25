/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.updateservice;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.updateservice.dto.SchemasRequestDTO;
import dk.dbc.updateservice.dto.SchemasResponseDTO;
import dk.dbc.updateservice.dto.UpdateRecordResponseDTO;
import dk.dbc.updateservice.dto.UpdateServiceRequestDTO;
import dk.dbc.util.Stopwatch;
import dk.dbc.dataio.commons.utils.lang.StringUtil;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateServiceUpdateConnector {
    JSONBContext jsonbContext = new JSONBContext();

    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceUpdateConnector.class);
    private static final String PATH_UPDATESERVICE = "/api/v1/updateservice";
    private static final String PATH_GETSCHEMAS = "/api/v1/updateservice/getschemas";

    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 404)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(1);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final UpdateServiceUpdateConnector.LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     */
    public UpdateServiceUpdateConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, UpdateServiceUpdateConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     * @param level      timings log level
     */
    public UpdateServiceUpdateConnector(Client httpClient, String baseUrl, UpdateServiceUpdateConnector.TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for update service endpoint
     */
    public UpdateServiceUpdateConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, UpdateServiceUpdateConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for update service endpoint
     * @param level              timings log level
     */
    public UpdateServiceUpdateConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, UpdateServiceUpdateConnector.TimingLogLevel level) {
        this.failSafeHttpClient = InvariantUtil.checkNotNullOrThrow(
                failSafeHttpClient, "failSafeHttpClient");
        this.baseUrl = InvariantUtil.checkNotNullNotEmptyOrThrow(
                baseUrl, "baseUrl");
        switch (level) {
            case TRACE:
                logger = LOGGER::trace;
                break;
            case DEBUG:
                logger = LOGGER::debug;
                break;
            case INFO:
                logger = LOGGER::info;
                break;
            case WARN:
                logger = LOGGER::warn;
                break;
            case ERROR:
                logger = LOGGER::error;
                break;
            default:
                logger = LOGGER::info;
                break;
        }
    }

    public UpdateRecordResponseDTO updateRecord(UpdateServiceRequestDTO updateServiceRequestDTO) throws UpdateServiceUpdateConnectorException, JSONBException {
        return updateRecord(updateServiceRequestDTO, null);
    }

    public UpdateRecordResponseDTO updateRecord(UpdateServiceRequestDTO updateServiceRequestDTO, String xForwardedFor) throws UpdateServiceUpdateConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream responseStream = sendPostRequest(PATH_UPDATESERVICE, updateServiceRequestDTO, xForwardedFor, InputStream.class);
            return jsonbContext.unmarshall(StringUtil.asString(responseStream), UpdateRecordResponseDTO.class);
        } finally {
            logger.log("updateRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    public SchemasResponseDTO getSchemas(SchemasRequestDTO schemasRequestDTO) throws UpdateServiceUpdateConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream responseStream = sendPostRequest(PATH_GETSCHEMAS, schemasRequestDTO, null, InputStream.class);
            return jsonbContext.unmarshall(StringUtil.asString(responseStream), SchemasResponseDTO.class);
        } finally {
            logger.log("getSchemas took {} milliseconds", stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequest(String basePath, Object request, String xForwardedFor, Class<T> type)
            throws UpdateServiceUpdateConnectorException, JSONBException {
        InvariantUtil.checkNotNullOrThrow(request, "request");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(jsonbContext.marshall(request), "application/json")
                .withHeader("Accept", "application/json")
                .withPathElements(path.build());

        if (xForwardedFor != null) {
            post.withHeader("X-Forwarded-For", xForwardedFor);
        }

        final Response response = post.execute();
        assertResponseStatus(response, Response.Status.OK);
        return readResponseEntity(response, type);
    }

    private <T> T readResponseEntity(Response response, Class<T> type)
            throws UpdateServiceUpdateConnectorException {
        final T entity = response.readEntity(type);
        if (entity == null) {
            throw new UpdateServiceUpdateConnectorException(
                    String.format("Update returned with null-valued %s entity",
                            type.getName()));
        }
        return entity;
    }

    private void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws UpdateServiceUpdateConnectorException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != expectedStatus) {
            throw new UpdateServiceUpdateConnectorException(
                    String.format("Update returned with '%s' status code: %s",
                            actualStatus,
                            actualStatus.getStatusCode()));
        }
    }

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }

}