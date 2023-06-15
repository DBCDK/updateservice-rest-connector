package dk.dbc.updateservice;

import dk.dbc.commons.jsonb.JSONBException;

public class UpdateServiceRestConnectorWireMockRecorder {

        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{RECORD_SERVICE_HOST}/UpdateService/rest" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Exception {
        UpdateServiceDoubleRecordCheckConnectorTest.connector = new UpdateServiceDoubleRecordCheckConnector(
                UpdateServiceDoubleRecordCheckConnectorTest.CLIENT, "http://localhost:8080");
        final UpdateServiceDoubleRecordCheckConnectorTest updateServiceDoubleRecordCheckConnectorTest = new UpdateServiceDoubleRecordCheckConnectorTest();

        UpdateServiceClassificationCheckConnectorTest.connector = new UpdateServiceClassificationCheckConnector(
                UpdateServiceClassificationCheckConnectorTest.CLIENT, "http://localhost:8080");
        final UpdateServiceClassificationCheckConnectorTest updateServiceClassificationCheckConnectorTest = new UpdateServiceClassificationCheckConnectorTest();

        UpdateServiceUpdateConnectorTest.connector = new UpdateServiceUpdateConnector(
                UpdateServiceUpdateConnectorTest.CLIENT, "http://localhost:8080");
        final UpdateServiceUpdateConnectorTest updateServiceUpdateConnectorTest = new UpdateServiceUpdateConnectorTest();

        doubleRecordCheckRequests(updateServiceDoubleRecordCheckConnectorTest);
        classificationCheckRequests(updateServiceClassificationCheckConnectorTest);
        updateRestRequests(updateServiceUpdateConnectorTest);
    }

    private static void doubleRecordCheckRequests(UpdateServiceDoubleRecordCheckConnectorTest connectorTest)
            throws Exception {
        connectorTest.checkDoubleRecordTest_Ok();
        connectorTest.checkDoubleRecordTest_DoubleRecord();
        connectorTest.checkDoubleRecordTest_DoubleRecord_DPF();
    }

    private static void classificationCheckRequests(UpdateServiceClassificationCheckConnectorTest connectorTest) throws Exception {
        connectorTest.classificationCheckTest_ClassificationChange();
        connectorTest.classificationCheckTest_NoClassificationChange();
    }

    private static void updateRestRequests(UpdateServiceUpdateConnectorTest connectorTest) throws JSONBException, UpdateServiceUpdateConnectorException {
        connectorTest.checkThatUpdateRecordWorksWithDTOS();
        connectorTest.checkThatConnectorReturnsProperAuthFailure();
        connectorTest.checkThatGetSchemasWorksWithDTOS();
    }
}
