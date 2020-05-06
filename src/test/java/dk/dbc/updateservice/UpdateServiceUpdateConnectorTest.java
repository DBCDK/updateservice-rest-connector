package dk.dbc.updateservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.jsonb.JSONBException;
import dk.dbc.updateservice.dto.AuthenticationDTO;
import dk.dbc.updateservice.dto.BibliographicRecordDTO;
import dk.dbc.updateservice.dto.MessageEntryDTO;
import dk.dbc.updateservice.dto.RecordDataDTO;
import dk.dbc.updateservice.dto.SchemaDTO;
import dk.dbc.updateservice.dto.SchemasRequestDTO;
import dk.dbc.updateservice.dto.SchemasResponseDTO;
import dk.dbc.updateservice.dto.TypeEnumDTO;
import dk.dbc.updateservice.dto.UpdateRecordResponseDTO;
import dk.dbc.updateservice.dto.UpdateServiceRequestDTO;
import dk.dbc.updateservice.dto.UpdateStatusEnumDTO;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.core.Is.is;

public class UpdateServiceUpdateConnectorTest {
    private static WireMockServer wireMockServer;
    private static String wireMockHost;
    static UpdateServiceUpdateConnector connector;

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
        connector = new UpdateServiceUpdateConnector(CLIENT, wireMockHost, UpdateServiceUpdateConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWiremockServer() { wireMockServer.stop(); }

    @Test
    void checkThatUpdateRecordWorksWithDTOS() throws JSONBException, UpdateServiceUpdateConnectorException {
        final UpdateServiceRequestDTO  updateServiceRequestDTO = getExampleRequest();

        final UpdateRecordResponseDTO expectedResponse = new UpdateRecordResponseDTO();
        expectedResponse.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.OK);

        UpdateRecordResponseDTO actualRespons = connector.updateRecord(updateServiceRequestDTO);

        assertThat("Update returns OK", actualRespons, is(expectedResponse));
    }

    @Test
    void checkThatGetSchemasWorksWithDTOS() throws JSONBException, UpdateServiceUpdateConnectorException {
        final SchemasRequestDTO schemasRequestDTO = getExampleRequestForSchemas();

        final SchemasResponseDTO expectedResponse = getExampleReponseForSchemas();

        SchemasResponseDTO actualResponse = connector.getSchemas(schemasRequestDTO);

        assertThat("Update (getschemas) returns OK", actualResponse, is(expectedResponse));
    }

    @Test
    void checkThatConnectorReturnsProperAuthFailure() throws JSONBException, UpdateServiceUpdateConnectorException {
        UpdateServiceRequestDTO updateServiceRequestDTO = getExampleRequest();
        updateServiceRequestDTO.getAuthenticationDTO().setGroupId("");

        final UpdateRecordResponseDTO expectedResponse = new UpdateRecordResponseDTO();
        expectedResponse.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.FAILED);
        MessageEntryDTO messageEntryDTO = new MessageEntryDTO();
        messageEntryDTO.setMessage("Webservice RESPONDS with content: Agency not found");
        messageEntryDTO.setType(TypeEnumDTO.FATAL);
        expectedResponse.addMessageEntryDtos(Arrays.asList(messageEntryDTO));

        UpdateRecordResponseDTO actualRespons = connector.updateRecord(updateServiceRequestDTO);

        assertThat("Update returns Failure, with proper message", actualRespons, is(expectedResponse));
    }

    private UpdateServiceRequestDTO getExampleRequest() {
        UpdateServiceRequestDTO updateServiceRequestDTO = new UpdateServiceRequestDTO();

        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setGroupId("010100");
        authenticationDTO.setPassword("");
        authenticationDTO.setUserId("");

        updateServiceRequestDTO.setAuthenticationDTO(authenticationDTO);
        updateServiceRequestDTO.setSchemaName("dbcautoritet");
        updateServiceRequestDTO.setTrackingId("update-warmup");

        BibliographicRecordDTO bibliographicRecordDTO = new BibliographicRecordDTO();
        bibliographicRecordDTO.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecordDTO.setRecordPacking("xml");

        RecordDataDTO recordDataDTO = new RecordDataDTO();
        List<Object> content = Arrays.asList("<record xmlns=\"info:lc/xmlns/marcxchange-v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd\">\n" +
                "                            <leader>dbfhfgh2</leader>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"001\">\n" +
                "                                <subfield code=\"a\">68693268</subfield>\n" +
                "                                <subfield code=\"b\">870979</subfield>\n" +
                "                                <subfield code=\"c\">20181108150337</subfield>\n" +
                "                                <subfield code=\"d\">20131129</subfield>\n" +
                "                                <subfield code=\"f\">a</subfield>\n" +
                "                                <subfield code=\"t\">faust</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"004\">\n" +
                "                                <subfield code=\"r\">n</subfield>\n" +
                "                                <subfield code=\"a\">e</subfield>\n" +
                "                                <subfield code=\"x\">n</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"008\">\n" +
                "                                <subfield code=\"t\">h</subfield>\n" +
                "                                <subfield code=\"v\">9</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"025\">\n" +
                "                                <subfield code=\"a\">5237167</subfield>\n" +
                "                                <subfield code=\"2\">viaf</subfield>\n" +
                "                                <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"025\">\n" +
                "                                <subfield code=\"a\">0000000013134949</subfield>\n" +
                "                                <subfield code=\"2\">isni</subfield>\n" +
                "                                <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"040\">\n" +
                "                                <subfield code=\"a\">DBC</subfield>\n" +
                "                                <subfield code=\"b\">dan</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"043\">\n" +
                "                                <subfield code=\"c\">dk</subfield>\n" +
                "                                <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"100\">\n" +
                "                                <subfield code=\"a\">Meilby</subfield>\n" +
                "                                <subfield code=\"h\">Mogens</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"375\">\n" +
                "                                <subfield code=\"a\">1</subfield>\n" +
                "                                <subfield code=\"2\">iso5218</subfield>\n" +
                "                                <subfield code=\"&amp;\">VIAF</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"d08\">\n" +
                "                                <subfield code=\"o\">autogenereret</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"xyz\">\n" +
                "                                <subfield code=\"u\">MEILBYMOGENS</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"z98\">\n" +
                "                                <subfield code=\"a\">Minus korrekturprint</subfield>\n" +
                "                            </datafield>\n" +
                "                            <datafield ind1=\"0\" ind2=\"0\" tag=\"z99\">\n" +
                "                                <subfield code=\"a\">VIAF</subfield>\n" +
                "                            </datafield>\n" +
                "                        </record>");
        recordDataDTO.setContent(content);
        bibliographicRecordDTO.setRecordDataDTO(recordDataDTO);
        updateServiceRequestDTO.setBibliographicRecordDTO(bibliographicRecordDTO);
        return updateServiceRequestDTO;
    }

    private SchemasRequestDTO getExampleRequestForSchemas() {
        SchemasRequestDTO schemasRequestDTO = new SchemasRequestDTO();
        AuthenticationDTO authenticationDTO = new AuthenticationDTO();
        authenticationDTO.setGroupId("010100");
        authenticationDTO.setUserId("");
        authenticationDTO.setPassword("");
        schemasRequestDTO.setAuthenticationDTO(authenticationDTO);
        schemasRequestDTO.setTrackingId("update-warmup");
        return schemasRequestDTO;
    }

    private SchemasResponseDTO getExampleReponseForSchemas() {
        SchemasResponseDTO schemasResponseDTO = new SchemasResponseDTO();
        schemasResponseDTO.setUpdateStatusEnumDTO(UpdateStatusEnumDTO.OK);
        List<SchemaDTO> schemaDTOs = Arrays.asList(
                getSchemaDTO("Skabelon til periodicaposter fra ffu-biblioteker.","ffuperiodica"),
                getSchemaDTO("","dbcsingle"),
                getSchemaDTO("", "dbcartanm"),
                getSchemaDTO("Skabelon til optrettelse af ffu-singlepost - alle materialetyper.","ffuhoved"),
                getSchemaDTO("Skabelon til emneordsposter, submitter: 190004", "dbcemneord"),
                getSchemaDTO("", "allowall"),
                getSchemaDTO("Skabelon til optrettelse af ffu-artikelpost (periodicaartikel og artikel i bog", "ffuartikel"),
                getSchemaDTO("", "invalid"),
                getSchemaDTO("Skabelon til autoritetsposter (libv3-base 870979)", "dbcautoritet"),
                getSchemaDTO("", "dbclittolk"),
                getSchemaDTO("Skabelon til katalogisering af flerbindsværk af fysiske bøger - bindpost.", "BCIbogbind"),
                getSchemaDTO("Skabelon til optrettelse af ffu-singlepost - alle materialetyper.", "ffu"),
                getSchemaDTO("", "dbchoved"),
                getSchemaDTO("", "dbcperiodica"),
                getSchemaDTO("Skabelon til materialevurderingsposter, submitter: 870976", "dbcmatvurd"),
                getSchemaDTO("Skabelon til katalogisering af fysiske bøger - enkeltstående post.", "BCIbog"),
                getSchemaDTO("Skabelon til sletteposter - alle post- og materialetyper.", "delete"),
                getSchemaDTO("Skabelon til optrettelse af ffu-sektionspost - alle materialetyper.", "ffusektion"),
                getSchemaDTO("Skabelon til indsendelse af metakompasdata til læsekompasset.", "metakompas"),
                getSchemaDTO("Skabelon til optrettelse af ffu-singlepost - alle materialetyper.", "ffumusik"),
                getSchemaDTO("", "dbcsektion"),
                getSchemaDTO("", "dbc"),
                getSchemaDTO("Skabelon til optrettelse af ffu-bindpost - alle materialetyper.", "ffubind"),
                getSchemaDTO("Skabelon til katalogisering af flerbindsværk af fysiske bøger - hovedpost.", "BCIboghoved"),
                getSchemaDTO("", "dbcbind")

        );
        schemasResponseDTO.setSchemaDTOList(schemaDTOs);
        return schemasResponseDTO;
    }

    private SchemaDTO getSchemaDTO(String schemaInfo, String schemaName) {
        SchemaDTO schemaDTO = new  SchemaDTO();
        schemaDTO.setSchemaInfo(schemaInfo);
        schemaDTO.setSchemaName(schemaName);
        return schemaDTO;
    }
}
