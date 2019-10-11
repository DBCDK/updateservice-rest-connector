/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.updateservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.updateservice.service.api.BibliographicRecord;
import dk.dbc.updateservice.service.api.RecordData;
import dk.dbc.updateservice.service.api.UpdateRecordResult;
import dk.dbc.updateservice.service.api.UpdateStatusEnum;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Client;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UpdateServiceDoubleRecordCheckConnectorTest {

    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));

    static UpdateServiceDoubleRecordCheckConnector connector;
    static DocumentBuilder documentBuilder;

    public UpdateServiceDoubleRecordCheckConnectorTest() {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(options().dynamicPort()
                .dynamicHttpsPort());
        wireMockServer.start();
        wireMockHost = "http://localhost:" + wireMockServer.port();
        configureFor("localhost", wireMockServer.port());
    }

    @BeforeAll
    static void setDocumentBuilder() {

    }

    @BeforeAll
    static void setConnector() {
        connector = new UpdateServiceDoubleRecordCheckConnector(CLIENT, wireMockHost, UpdateServiceDoubleRecordCheckConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void checkDoubleRecordTest_Ok() throws Exception {
        BibliographicRecord bibliographicRecord = new BibliographicRecord();
        bibliographicRecord.setRecordSchema("info:lc/xmlns/marcxchange-v1</recordSchema");
        bibliographicRecord.setRecordPacking("xml");

        String recordString = "        <record>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">" +
                "                <subfield code=\"a\">52958858</subfield>" +
                "                <subfield code=\"b\">870970</subfield>" +
                "                <subfield code=\"c\">20170616143600</subfield>" +
                "                <subfield code=\"d\">20180628</subfield>" +
                "                <subfield code=\"f\">a</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">" +
                "                <subfield code=\"r\">n</subfield>" +
                "                <subfield code=\"a\">e</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">" +
                "                <subfield code=\"t\">m</subfield>" +
                "                <subfield code=\"u\">f</subfield>" +
                "                <subfield code=\"a\">2016</subfield>" +
                "                <subfield code=\"b\">sy</subfield>" +
                "                <subfield code=\"d\">x</subfield>" +
                "                <subfield code=\"j\">f</subfield>" +
                "                <subfield code=\"l\">ara</subfield>" +
                "                <subfield code=\"v\">0</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">" +
                "                <subfield code=\"a\">a</subfield>" +
                "                <subfield code=\"g\">xx</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">" +
                "                <subfield code=\"e\">9782843090387</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"041\">" +
                "                <subfield code=\"a\">ara</subfield>" +
                "                <subfield code=\"c\">per</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">" +
                "                <subfield code=\"a\">A̕lavı̄</subfield>" +
                "                <subfield code=\"h\">Buzurg</subfield>" +
                "                <subfield code=\"4\">aut</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"241\">" +
                "                <subfield code=\"a\">Chashmhāyesh</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">" +
                "                <subfield code=\"a\">ʻAynāhā</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">" +
                "                <subfield code=\"a\">Damaskus</subfield>" +
                "                <subfield code=\"b\">Dār al-Madā</subfield>" +
                "                <subfield code=\"c\">2016</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">" +
                "                <subfield code=\"a\">266 sider</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"504\">" +
                "                <subfield code=\"a\">En mand tænker på en pige - og især hendes øjne</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"505\">" +
                "                <subfield code=\"a\">Roman</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">" +
                "                <subfield code=\"m\">88.846</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"720\">" +
                "                <subfield code=\"o\">Aḥmad Ḥaydarī</subfield>" +
                "                <subfield code=\"4\">trl</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">" +
                "                <subfield code=\"a\">700300</subfield>" +
                "            </datafield>" +
                "        </record>";

        RecordData recordData = new RecordData();
        Document document = byteArrayToDocument(recordString.getBytes());
        recordData.getContent().add(document.getDocumentElement());
        bibliographicRecord.setRecordData(recordData);

        UpdateRecordResult actual = connector.doubleRecordCheck(bibliographicRecord);
        UpdateRecordResult expected = new UpdateRecordResult();
        expected.setUpdateStatus(UpdateStatusEnum.OK);

        assertThat("Double record check returns OK if there is no match", actual, is(expected));
    }

    private Document byteArrayToDocument(byte[] byteArray) throws IOException, SAXException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        documentBuilder.reset();
        return documentBuilder.parse(byteArrayInputStream);
    }

}
