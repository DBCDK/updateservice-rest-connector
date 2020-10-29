/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.updateservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.marc.reader.MarcXchangeV1Reader;
import dk.dbc.marc.writer.MarcXchangeV1Writer;
import dk.dbc.updateservice.dto.BibliographicRecordDTO;
import dk.dbc.updateservice.dto.DoubleRecordFrontendDTO;
import dk.dbc.updateservice.dto.RecordDataDTO;
import dk.dbc.updateservice.dto.UpdateRecordResponseDTO;
import dk.dbc.updateservice.dto.UpdateStatusEnumDTO;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1</recordSchema");
        bibliographicRecordDTO.setRecordPacking("xml");

        String recordString = "        <record xmlns=\"info:lc/xmlns/marcxchange-v1\">" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">" +
                "                <subfield code=\"a\">52958858</subfield>" +
                "                <subfield code=\"b\">870970</subfield>" +
                "                <subfield code=\"c\">20170616143600</subfield>" +
                "                <subfield code=\"d\">20180628</subfield>" +
                "                <subfield code=\"f\">a</subfield>" +
                "            </datafield>" +
                "        </record>";

        List<Object> content = Arrays.asList(byteArrayToDocument(recordString.getBytes()));

        RecordDataDTO recordDataDTO = new RecordDataDTO();
        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);

        UpdateRecordResponseDTO actual = connector.doubleRecordCheck(bibliographicRecordDTO);
        UpdateRecordResponseDTO expected = new UpdateRecordResponseDTO();
        expected.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.OK);

        assertThat("Double record check returns OK if there is no match", actual, is(expected));
    }

    @Test
    void checkDoubleRecordTest_DoubleRecord() throws Exception {
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1</recordSchema");
        bibliographicRecordDTO.setRecordPacking("xml");

        String recordString = "        <record xmlns=\"info:lc/xmlns/marcxchange-v1\">" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">" +
                "                <subfield code=\"a\">52958858</subfield>" +
                "                <subfield code=\"b\">870970</subfield>" +
                "                <subfield code=\"c\">20170616143600</subfield>" +
                "                <subfield code=\"d\">20180628</subfield>" +
                "                <subfield code=\"f\">a</subfield>" +
                "            </datafield>" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">" +
                "                <subfield code=\"e\">9782843090387</subfield>" +
                "            </datafield>" +
                "        </record>";

        List<Object> content = Arrays.asList(byteArrayToDocument(recordString.getBytes()));
        RecordDataDTO recordDataDTO = new RecordDataDTO();
        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);

        System.out.println("Request is:" + new JSONBContext().marshall(bibliographicRecordDTO));

        UpdateRecordResponseDTO actual = connector.doubleRecordCheck(bibliographicRecordDTO);
        UpdateRecordResponseDTO expected = new UpdateRecordResponseDTO();
        expected.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.FAILED);
        expected.setDoubleRecordKey("52743f23-0522-40d7-b762-557fc717160b");

        DoubleRecordFrontendDTO doubleRecordFrontendDTO = new DoubleRecordFrontendDTO();
        doubleRecordFrontendDTO.setMessage("Double record for record 52958858, reason: 021e");
        doubleRecordFrontendDTO.setPid("52958857:870970");

        List<DoubleRecordFrontendDTO> doubleRecordEntries = new ArrayList<>();

        doubleRecordEntries.add(doubleRecordFrontendDTO);

        expected.addDoubleRecordFrontendDtos(doubleRecordEntries);

        assertThat("Double record check returns failed when double record is detected", actual, is(expected));
    }

    @Test
    void checkDoubleRecordTest_DoubleRecord_DPF() throws Exception {
        String recordString = "    <record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "        <leader>00000nas a2200000 i 4500</leader>\n" +
                "        <datafield ind1=\"0\" ind2=\" \" tag=\"022\">\n" +
                "            <subfield code=\"a\">2597-0378</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\"0\" ind2=\"4\" tag=\"082\">\n" +
                "            <subfield code=\"a\">700</subfield>\n" +
                "            <subfield code=\"2\">21</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\"1\" ind2=\"0\" tag=\"245\">\n" +
                "            <subfield code=\"a\">Professor Wilfred Christensen :</subfield>\n" +
                "            <subfield code=\"b\">tidsskrift : star wars og star trek.</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\"3\" ind2=\" \" tag=\"260\">\n" +
                "            <subfield code=\"a\">København :</subfield>\n" +
                "            <subfield code=\"b\">The Package,</subfield>\n" +
                "            <subfield code=\"c\">2019-</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\"0\" ind2=\" \" tag=\"362\">\n" +
                "            <subfield code=\"a\">Årgang 2019, oktober-</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\" \" ind2=\" \" tag=\"912\">\n" +
                "            <subfield code=\"m\">70.6</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\" \" ind2=\" \" tag=\"932\">\n" +
                "            <subfield code=\"a\">DPF</subfield>\n" +
                "            <subfield code=\"b\">202018</subfield>\n" +
                "            <subfield code=\"c\">NY</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
                "            <subfield code=\"a\">(DK-800010)12345678969005763</subfield>\n" +
                "        </datafield>\n" +
                "        <datafield ind1=\" \" ind2=\" \" tag=\"310\">\n" +
                "            <subfield code=\"a\">2 gange om året</subfield>\n" +
                "        </datafield>\n" +
                "    </record>";


        MarcRecord marcRecord = fromMarcXchange(recordString.getBytes());

        final byte[] content = toMarcXchange(marcRecord);
        final RecordDataDTO recordDataDTO = new RecordDataDTO();
        recordDataDTO.setContent(Collections.singletonList(new String(content)));

        final BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1</recordSchema");
        bibliographicRecordDTO.setRecordPacking("xml");
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);

        System.out.println("Request is:" + new JSONBContext().marshall(bibliographicRecordDTO));

        UpdateRecordResponseDTO actual = connector.doubleRecordCheck(bibliographicRecordDTO);
        UpdateRecordResponseDTO expected = new UpdateRecordResponseDTO();
        expected.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.OK);

        assertThat("Double record check returns OK if there is no match", actual, is(expected));
    }

    private Document byteArrayToDocument(byte[] byteArray) throws IOException, SAXException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        documentBuilder.reset();
        return documentBuilder.parse(byteArrayInputStream);
    }

    private static MarcRecord fromMarcXchange(byte[] bytes) throws MarcReaderException {
        final MarcXchangeV1Reader reader = new MarcXchangeV1Reader(
                new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
        return reader.read();
    }

    private static byte[] toMarcXchange(MarcRecord marcRecord) {
        final MarcXchangeV1Writer writer = new MarcXchangeV1Writer();
        return writer.write(marcRecord, StandardCharsets.UTF_8);
    }

}
