package dk.dbc.updateservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.updateservice.dto.BibliographicRecordDTO;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import dk.dbc.updateservice.dto.RecordDataDTO;
import dk.dbc.updateservice.dto.TypeEnumDTO;
import dk.dbc.updateservice.dto.UpdateRecordResponseDTO;
import dk.dbc.updateservice.dto.UpdateStatusEnumDTO;
import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UpdateServiceClassificationCheckConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));

    static UpdateServiceClassificationCheckConnector connector;
    static DocumentBuilder documentBuilder;

    public UpdateServiceClassificationCheckConnectorTest() {
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
    static void setConnector() {
        connector = new UpdateServiceClassificationCheckConnector(CLIENT, wireMockHost, UpdateServiceClassificationCheckConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void classificationCheckTest_NoClassificationChange() throws Exception {
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecordDTO.setRecordPacking("xml");
        String record = "<record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "            <leader>00000n    2200000   4500</leader>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "                <subfield code=\"a\">50938409</subfield>\n" +
                "                <subfield code=\"b\">870970</subfield>\n" +
                "                <subfield code=\"c\">20191218013539</subfield>\n" +
                "                <subfield code=\"d\">20140131</subfield>\n" +
                "                <subfield code=\"f\">a</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "                <subfield code=\"r\">n</subfield>\n" +
                "                <subfield code=\"a\">e</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "                <subfield code=\"t\">m</subfield>\n" +
                "                <subfield code=\"u\">f</subfield>\n" +
                "                <subfield code=\"a\">2014</subfield>\n" +
                "                <subfield code=\"b\">dk</subfield>\n" +
                "                <subfield code=\"d\">2</subfield>\n" +
                "                <subfield code=\"d\">å</subfield>\n" +
                "                <subfield code=\"d\">x</subfield>\n" +
                "                <subfield code=\"l\">dan</subfield>\n" +
                "                <subfield code=\"o\">b</subfield>\n" +
                "                <subfield code=\"v\">0</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">\n" +
                "                <subfield code=\"a\">a</subfield>\n" +
                "                <subfield code=\"g\">xx</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "                <subfield code=\"e\">9788792286376</subfield>\n" +
                "                <subfield code=\"c\">ib.</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"032\">\n" +
                "                <subfield code=\"a\">DBF201409</subfield>\n" +
                "                <subfield code=\"x\">BKM201409</subfield>\n" +
                "                <subfield code=\"x\">ACC201405</subfield>\n" +
                "                <subfield code=\"x\">DAT991605</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"038\">\n" +
                "                <subfield code=\"a\">bi</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"041\">\n" +
                "                <subfield code=\"a\">dan</subfield>\n" +
                "                <subfield code=\"c\">nor</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "                <subfield code=\"5\">870979</subfield>\n" +
                "                <subfield code=\"6\">69208045</subfield>\n" +
                "                <subfield code=\"4\">aut</subfield>\n" +
                "                <subfield code=\"4\">art</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"241\">\n" +
                "                <subfield code=\"a\">Odd er et egg</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
                "                <subfield code=\"a\">Ib er et æg</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"250\">\n" +
                "                <subfield code=\"a\">1. udgave</subfield>\n" +
                "                <subfield code=\"b\">÷</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">\n" +
                "                <subfield code=\"&amp;\">1</subfield>\n" +
                "                <subfield code=\"a\">Hedehusene</subfield>\n" +
                "                <subfield code=\"b\">Torgard</subfield>\n" +
                "                <subfield code=\"c\">2014</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">\n" +
                "                <subfield code=\"a\">[36] sider</subfield>\n" +
                "                <subfield code=\"b\">alle ill. i farver</subfield>\n" +
                "                <subfield code=\"c\">28 cm</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"504\">\n" +
                "                <subfield code=\"&amp;\">1</subfield>\n" +
                "                <subfield code=\"a\">Billedbog. Hver morgen pakker Ib sit hoved ind i håndklæder og en tehætte. Hans hoved er nemlig et æg, og han skal hele tiden passe på, at det ikke går i stykker. Men så møder han Sif. Hun passer ikke på noget</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"521\">\n" +
                "                <subfield code=\"&amp;\">REX</subfield>\n" +
                "                <subfield code=\"b\">1. oplag</subfield>\n" +
                "                <subfield code=\"c\">2014</subfield>\n" +
                "                <subfield code=\"k\">Arcorounborg</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "                <subfield code=\"n\">85</subfield>\n" +
                "                <subfield code=\"z\">296</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "                <subfield code=\"o\">sk</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">alene</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">ensomhed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">venskab</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">kærlighed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">tapperhed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">mod</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 4 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 5 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 6 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 7 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"720\">\n" +
                "                <subfield code=\"o\">Hugin Eide</subfield>\n" +
                "                <subfield code=\"4\">trl</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"990\">\n" +
                "                <subfield code=\"o\">201409</subfield>\n" +
                "                <subfield code=\"b\">l</subfield>\n" +
                "                <subfield code=\"b\">b</subfield>\n" +
                "                <subfield code=\"b\">s</subfield>\n" +
                "                <subfield code=\"u\">nt</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">\n" +
                "                <subfield code=\"a\">DBC</subfield>\n" +
                "            </datafield>\n" +
                "        </record>";

        List<Object> content = Collections.singletonList(byteArrayToDocument(record.getBytes()));

        RecordDataDTO recordDataDTO = new RecordDataDTO();
        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);
        UpdateRecordResponseDTO actual = connector.classificationCheck(bibliographicRecordDTO);
        UpdateRecordResponseDTO expected = new UpdateRecordResponseDTO();
        expected.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.OK);
        assertThat("Classification check returns OK if there is no classification change", actual, is(expected));
    }

    @Test
    void classificationCheckTest_ClassificationChange() throws Exception {
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecordDTO.setRecordPacking("xml");

        String record = "<record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "            <leader>00000n    2200000   4500</leader>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "                <subfield code=\"a\">50938409</subfield>\n" +
                "                <subfield code=\"b\">870970</subfield>\n" +
                "                <subfield code=\"c\">20191218013539</subfield>\n" +
                "                <subfield code=\"d\">20140131</subfield>\n" +
                "                <subfield code=\"f\">a</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "                <subfield code=\"r\">n</subfield>\n" +
                "                <subfield code=\"a\">e</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "                <subfield code=\"t\">m</subfield>\n" +
                "                <subfield code=\"u\">f</subfield>\n" +
                "                <subfield code=\"a\">2014</subfield>\n" +
                "                <subfield code=\"b\">dk</subfield>\n" +
                "                <subfield code=\"d\">2</subfield>\n" +
                "                <subfield code=\"d\">å</subfield>\n" +
                "                <subfield code=\"d\">x</subfield>\n" +
                "                <subfield code=\"l\">dan</subfield>\n" +
                "                <subfield code=\"o\">b</subfield>\n" +
                "                <subfield code=\"v\">0</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"009\">\n" +
                "                <subfield code=\"a\">a</subfield>\n" +
                "                <subfield code=\"g\">xx</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"021\">\n" +
                "                <subfield code=\"e\">9788792286376</subfield>\n" +
                "                <subfield code=\"c\">ib.</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"032\">\n" +
                "                <subfield code=\"a\">DBF201409</subfield>\n" +
                "                <subfield code=\"x\">BKM201409</subfield>\n" +
                "                <subfield code=\"x\">ACC201405</subfield>\n" +
                "                <subfield code=\"x\">DAT991605</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"038\">\n" +
                "                <subfield code=\"a\">bi</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"041\">\n" +
                "                <subfield code=\"a\">dan</subfield>\n" +
                "                <subfield code=\"c\">nor</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "                <subfield code=\"5\">870979</subfield>\n" +
                "                <subfield code=\"6\">69208045</subfield>\n" +
                "                <subfield code=\"4\">aut</subfield>\n" +
                "                <subfield code=\"4\">art</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"241\">\n" +
                "                <subfield code=\"a\">Odd er et egg</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
                "                <subfield code=\"a\">Ib er et æggehoved</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"250\">\n" +
                "                <subfield code=\"a\">1. udgave</subfield>\n" +
                "                <subfield code=\"b\">÷</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"260\">\n" +
                "                <subfield code=\"&amp;\">1</subfield>\n" +
                "                <subfield code=\"a\">Hedehusene</subfield>\n" +
                "                <subfield code=\"b\">Torgard</subfield>\n" +
                "                <subfield code=\"c\">2014</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"300\">\n" +
                "                <subfield code=\"a\">[36] sider</subfield>\n" +
                "                <subfield code=\"b\">alle ill. i farver</subfield>\n" +
                "                <subfield code=\"c\">28 cm</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"504\">\n" +
                "                <subfield code=\"&amp;\">1</subfield>\n" +
                "                <subfield code=\"a\">Billedbog. Hver morgen pakker Ib sit hoved ind i håndklæder og en tehætte. Hans hoved er nemlig et æg, og han skal hele tiden passe på, at det ikke går i stykker. Men så møder han Sif. Hun passer ikke på noget</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"521\">\n" +
                "                <subfield code=\"&amp;\">REX</subfield>\n" +
                "                <subfield code=\"b\">1. oplag</subfield>\n" +
                "                <subfield code=\"c\">2014</subfield>\n" +
                "                <subfield code=\"k\">Arcorounborg</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "                <subfield code=\"n\">85</subfield>\n" +
                "                <subfield code=\"z\">296</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"652\">\n" +
                "                <subfield code=\"o\">sk</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">alene</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">ensomhed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">venskab</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">kærlighed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">tapperhed</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"s\">mod</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 4 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 5 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 6 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"666\">\n" +
                "                <subfield code=\"0\"/>\n" +
                "                <subfield code=\"u\">for 7 år</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"720\">\n" +
                "                <subfield code=\"o\">Hugin Eide</subfield>\n" +
                "                <subfield code=\"4\">trl</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"990\">\n" +
                "                <subfield code=\"o\">201409</subfield>\n" +
                "                <subfield code=\"b\">l</subfield>\n" +
                "                <subfield code=\"b\">b</subfield>\n" +
                "                <subfield code=\"b\">s</subfield>\n" +
                "                <subfield code=\"u\">nt</subfield>\n" +
                "            </datafield>\n" +
                "            <datafield ind1=\"0\" ind2=\"0\" tag=\"996\">\n" +
                "                <subfield code=\"a\">DBC</subfield>\n" +
                "            </datafield>\n" +
                "        </record>";

        List<Object> content = Collections.singletonList(byteArrayToDocument(record.getBytes()));

        RecordDataDTO recordDataDTO = new RecordDataDTO();
        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);

        UpdateRecordResponseDTO actual = connector.classificationCheck(bibliographicRecordDTO);
        UpdateRecordResponseDTO expected = new UpdateRecordResponseDTO();
        expected.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.FAILED);

        List<MessageEntryDTO> messageEntries = new ArrayList<>();

        MessageEntryDTO holdingsEntry = new MessageEntryDTO();
        holdingsEntry.setType(TypeEnumDTO.WARNING);
        holdingsEntry.setMessage("Count: 8");
        messageEntries.add(holdingsEntry);

        MessageEntryDTO classificationEntry = new MessageEntryDTO();
        classificationEntry.setType(TypeEnumDTO.WARNING);
        classificationEntry.setMessage("Reason: 245a er ændret");
        messageEntries.add(classificationEntry);

        expected.addMessageEntryDtos(messageEntries);
        assertThat("Classification check returns OK if there is no classification change", actual, is(expected));
    }

    private Document byteArrayToDocument(byte[] byteArray) throws IOException, SAXException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        documentBuilder.reset();
        return documentBuilder.parse(byteArrayInputStream);
    }
}
