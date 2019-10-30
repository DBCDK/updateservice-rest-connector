/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.updateservice;

import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpPost;
import dk.dbc.httpclient.PathBuilder;
import dk.dbc.invariant.InvariantUtil;
import dk.dbc.oss.ns.catalogingupdate.BibliographicRecord;
import dk.dbc.oss.ns.catalogingupdate.ObjectFactory;
import dk.dbc.oss.ns.catalogingupdate.UpdateRecordResult;
import dk.dbc.util.Stopwatch;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.io.StringWriter;
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

    public UpdateRecordResult doubleRecordCheck(BibliographicRecord bibliographicRecord) throws UpdateServiceDoubleRecordCheckConnectorException {
        final Stopwatch stopwatch = new Stopwatch();
        try {
            final InputStream inputStream = sendPostRequest(PATH_DOUBLE_RECORD_CHECK, bibliographicRecord, InputStream.class);
            return JAXB.unmarshal(inputStream, UpdateRecordResult.class);
        } finally {
            logger.log("doubleRecordCheck took {} milliseconds",
                    stopwatch.getElapsedTime(TimeUnit.MILLISECONDS));
        }
    }

    private <T> T sendPostRequest(String basePath, BibliographicRecord bibliographicRecord, Class<T> type)
            throws UpdateServiceDoubleRecordCheckConnectorException {
        InvariantUtil.checkNotNullOrThrow(bibliographicRecord, "bibliographicRecord");
        final PathBuilder path = new PathBuilder(basePath);
        final HttpPost post = new HttpPost(failSafeHttpClient)
                .withBaseUrl(baseUrl)
                .withData(marshal(bibliographicRecord), "application/xml")
                .withHeader("Accept", "application/xml")
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

    String marshal(BibliographicRecord bibliographicRecord) {
        try {
            final ObjectFactory objectFactory = new ObjectFactory();
            final JAXBElement<BibliographicRecord> jAXBElement = objectFactory.createBibliographicRecord(bibliographicRecord);
            final StringWriter stringWriter = new StringWriter();
            final JAXBContext jaxbContext = JAXBContext.newInstance(BibliographicRecord.class);
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(jAXBElement, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            LOGGER.warn("Got an error while marshalling input bibliographicRecord, using reflection instead.", e);
            return new ReflectionToStringBuilder(bibliographicRecord, new RecursiveToStringStyle()).toString();
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
