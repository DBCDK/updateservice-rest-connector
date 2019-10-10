package dk.dbc.updateservice;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.invariant.InvariantUtil;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class UpdateServiceDoubleRecordCheckConnector {
    public enum TimingLogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceDoubleRecordCheckConnector.class);
    private static final String PATH_DOUBLE_RECORD_CHECK = "/api/v1/doublerecordcheck";

    private static final RetryPolicy RETRY_POLICY = new RetryPolicy()
            .retryOn(Collections.singletonList(ProcessingException.class))
            .retryIf((Response response) -> response.getStatus() == 404)
            .withDelay(10, TimeUnit.SECONDS)
            .withMaxRetries(1);

    private final FailSafeHttpClient failSafeHttpClient;
    private final String baseUrl;
    private final UpdateServiceDoubleRecordCheckConnector.LogLevelMethod logger;

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for record service endpoint
     */
    public UpdateServiceDoubleRecordCheckConnector(Client httpClient, String baseUrl) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with default retry policy
     *
     * @param httpClient web resources client
     * @param baseUrl    base URL for record service endpoint
     * @param level      timings log level
     */
    public UpdateServiceDoubleRecordCheckConnector(Client httpClient, String baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel level) {
        this(FailSafeHttpClient.create(httpClient, RETRY_POLICY), baseUrl, level);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for record service endpoint
     */
    public UpdateServiceDoubleRecordCheckConnector(FailSafeHttpClient failSafeHttpClient, String baseUrl) {
        this(failSafeHttpClient, baseUrl, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel.INFO);
    }

    /**
     * Returns new instance with custom retry policy
     *
     * @param failSafeHttpClient web resources client with custom retry policy
     * @param baseUrl            base URL for record service endpoint
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

    public void close() {
        failSafeHttpClient.getClient().close();
    }

    @FunctionalInterface
    interface LogLevelMethod {
        void log(String format, Object... objs);
    }

}
