package dk.dbc.updateservice;

import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;
import dk.dbc.dataio.commons.utils.lang.StringUtil;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.updateservice.dto.BibliographicRecordDTO;
import dk.dbc.updateservice.dto.UpdateRecordResponseDTO;
import dk.dbc.util.Stopwatch;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


public class UpdateServiceDoubleRecordCheckConnector {
    private final JSONBContext jsonbContext = new JSONBContext();

    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceDoubleRecordCheckConnector.class);
    private static final String PATH_DOUBLE_RECORD_CHECK = "/api/v2/doublerecordcheck";

    private static final RetryPolicy<Response> RETRY_POLICY = new RetryPolicy<Response>()
            .handle(ProcessingException.class)
            .handleResultIf(response -> response.getStatus() == 404)
            .withDelay(Duration.ofSeconds(10))
            .withMaxRetries(1);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final UpdateServiceDoubleRecordCheckConnector.LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     */
    public UpdateServiceDoubleRecordCheckConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for update service endpoint
     * @param level      timings log level
     */
    public UpdateServiceDoubleRecordCheckConnector(Client httpClient, String baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for update service endpoint
     */
    public UpdateServiceDoubleRecordCheckConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for update service endpoint
     * @param level              timings log level
     */
    public UpdateServiceDoubleRecordCheckConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel level) {
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

    public UpdateRecordResponseDTO doubleRecordCheck(BibliographicRecordDTO bibliographicRecordDTO) throws UpdateServiceDoubleRecordCheckConnectorException, JSONBException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream inputStream = sendPostRequest(PATH_DOUBLE_RECORD_CHECK, bibliographicRecordDTO, InputStream.class);
            return jsonbContext.unmarshall(StringUtil.asString(inputStream), UpdateRecordResponseDTO.class);
        } finally {
            logger.log("doubleRecordCheck took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequest(String basePath, BibliographicRecordDTO bibliographicRecordDTO, Class<T> type)
            throws UpdateServiceDoubleRecordCheckConnectorException, JSONBException {
        InvariantUtil.checkNotNullOrThrow(bibliographicRecordDTO, "bibliographicRecord");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(jsonbContext.marshall(bibliographicRecordDTO), "application/json")
                .withHeader("Accept", "application/json")
                .withPathElements(path.build());

        final Response response = post.execute();
        assertResponseStatus(response, Response.Status.OK);
        return readResponseEntity(response, type);
    }

    private <T> T readResponseEntity(Response response, Class<T> type)
            throws UpdateServiceDoubleRecordCheckConnectorException {
        final T entity = response.readEntity(type);
        if (entity == null) {
            throw new UpdateServiceDoubleRecordCheckConnectorException(
                    String.format("Double record check returned with null-valued %s entity",
                            type.getName()));
        }
        return entity;
    }

    private void assertResponseStatus(Response response, Response.Status expectedStatus)
            throws UpdateServiceDoubleRecordCheckConnectorUnexpectedStatusCodeException {
        final Response.Status actualStatus =
                Response.Status.fromStatusCode(response.getStatus());
        if (actualStatus != expectedStatus) {
            throw new UpdateServiceDoubleRecordCheckConnectorUnexpectedStatusCodeException(
                    String.format("Double record check returned with unexpected status code: %s",
                            actualStatus),
                    actualStatus.getStatusCode());
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
