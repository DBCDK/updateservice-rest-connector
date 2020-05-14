package dk.dbc.updateservice;

import dk.dbc.dataio.commons.utils.lang.StringUtil;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.updateservice.dto.BuildRequestDTO;
import dk.dbc.updateservice.dto.BuildResponseDTO;
import dk.dbc.util.Stopwatch;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateServiceBuildConnector {
    JSONBContext jsonbContext = new JSONBContext();
    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceBuildConnector.class);
    private static final String PATH_BUILDSERVICE = "/api/v1/openbuildservice";

    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 404)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(1);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final UpdateServiceBuildConnector.LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     */
    public UpdateServiceBuildConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, UpdateServiceBuildConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     * @param level      timings log level
     */
    public UpdateServiceBuildConnector(Client httpClient, String baseUrl, UpdateServiceBuildConnector.TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * UpdateServiceBuildConnector@param baseUrl            base URL for update service endpoint
     */
    public UpdateServiceBuildConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, UpdateServiceBuildConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for update service endpoint
     * @param level              timings log level
     */
    public UpdateServiceBuildConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, UpdateServiceBuildConnector.TimingLogLevel level) {
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

    public BuildResponseDTO buildRecord(BuildRequestDTO buildRequestDTO) throws UpdateServiceBuildConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream responseStream = sendPostRequest(PATH_BUILDSERVICE, buildRequestDTO, InputStream.class);
            return jsonbContext.unmarshall(StringUtil.asString(responseStream), BuildResponseDTO.class);
        } finally {
            logger.log("buildRecord took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequest(String basePath, BuildRequestDTO buildRequestDTO, Class<T> type)
            throws UpdateServiceBuildConnectorException, JSONBException {
        InvariantUtil.checkNotNullOrThrow(buildRequestDTO, "buildRequestDTO");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(jsonbContext.marshall(buildRequestDTO), "application/json")
                .withHeader("Accept", "application/json")
                .withPathElements(path.build());

        final Response response = post.execute();
        assertResponseStatus(response, Response.Status.OK);
        return readResponseEntity(response, type);
    }

    private <T> T readResponseEntity(Response response, Class<T> type)
            throws UpdateServiceBuildConnectorException {
        final T entity = response.readEntity(type);
        if (entity == null) {
            throw new UpdateServiceBuildConnectorException(
                    String.format("Build returned with null-valued %s entity",
                            type.getName()));
        }
        return entity;
    }

    private void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws UpdateServiceBuildConnectorException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != expectedStatus) {
            throw new UpdateServiceBuildConnectorException(
                    String.format("Build returned with '%s' status code: %s",
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
