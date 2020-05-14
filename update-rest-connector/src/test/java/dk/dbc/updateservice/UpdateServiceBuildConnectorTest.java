package dk.dbc.updateservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.updateservice.dto.BibliographicRecordDTO;
import dk.dbc.updateservice.dto.BuildRequestDTO;
import dk.dbc.updateservice.dto.BuildResponseDTO;
import dk.dbc.updateservice.dto.BuildStatusEnumDTO;
import dk.dbc.updateservice.dto.ExtraRecordDataDTO;
import dk.dbc.updateservice.dto.RecordDataDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UpdateServiceBuildConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;
    static UpdateServiceBuildConnector connector;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));

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
        connector = new UpdateServiceBuildConnector(CLIENT, wireMockHost, UpdateServiceBuildConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWiremockServer() { wireMockServer.stop(); }

    @Test
    void checkThatConnectorWorksWithDTOS() throws JSONBException, UpdateServiceBuildConnectorException {
        final BuildRequestDTO buildRequestDTO = getExampleRequest();

        final BuildResponseDTO expectedResponse = getExampleResponse();

        BuildResponseDTO actualRespons = connector.buildRecord(buildRequestDTO);

        assertThat("Build returns valid new record", actualRespons,
                is(expectedResponse));
    }

    private BuildRequestDTO getExampleRequest() {
        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setSchemaName("ffu");
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecordDTO.setRecordPacking("xml");
        RecordDataDTO recordDataDTO = new RecordDataDTO();
        List<Object> content = new ArrayList<>();
        recordDataDTO.setContent(content);
        ExtraRecordDataDTO extraRecordDataDTO = new ExtraRecordDataDTO();
        extraRecordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);
        bibliographicRecordDTO.setExtraRecordDataDTO(extraRecordDataDTO);
        buildRequestDTO.setBibliographicRecordDTO(bibliographicRecordDTO);
        buildRequestDTO.setTrackingId("atm-manual");
        return buildRequestDTO;
    }

    private BuildResponseDTO getExampleResponse() {
        BuildResponseDTO buildResponseDTO = new BuildResponseDTO();
        buildResponseDTO.setBuildStatusEnumDTO(BuildStatusEnumDTO.OK);
        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecordDTO.setRecordPacking("xml");
        RecordDataDTO recordDataDTO = new RecordDataDTO();
        List<Object> content = Arrays.asList( "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\"><leader>00000n    2200000   4500</leader><datafield ind1=\"0\" ind2=\"0\" tag=\"001\"><subfield code=\"a\">126808879</subfield><subfield code=\"b\"></subfield><subfield code=\"f\">a</subfield></datafield><datafield ind1=\"0\" ind2=\"0\" tag=\"004\"><subfield code=\"r\"></subfield><subfield code=\"a\">e</subfield></datafield><datafield ind1=\"0\" ind2=\"0\" tag=\"008\"><subfield code=\"v\"></subfield></datafield><datafield ind1=\"0\" ind2=\"0\" tag=\"009\"><subfield code=\"a\"></subfield></datafield><datafield ind1=\"0\" ind2=\"0\" tag=\"096\"><subfield code=\"z\"></subfield></datafield><datafield ind1=\"0\" ind2=\"0\" tag=\"245\"><subfield code=\"a\"></subfield></datafield></record>");

        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);
        buildResponseDTO.setBibliographicRecordDTO(bibliographicRecordDTO);
        buildResponseDTO.setBuildStatusEnumDTO(BuildStatusEnumDTO.OK);
        return buildResponseDTO;
    }

}
