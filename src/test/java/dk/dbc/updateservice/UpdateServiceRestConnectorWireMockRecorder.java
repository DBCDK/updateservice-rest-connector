package dk.dbc.updateservice;

public class UpdateServiceRestConnectorWireMockRecorder {

        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{RECORD_SERVICE_HOST}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Exception {
        UpdateServiceDoubleRecordCheckConnectorTest.connector = new UpdateServiceDoubleRecordCheckConnector(
                UpdateServiceDoubleRecordCheckConnectorTest.CLIENT, "http://localhost:8080");
        final UpdateServiceDoubleRecordCheckConnectorTest updateServiceDoubleRecordCheckConnectorTest = new UpdateServiceDoubleRecordCheckConnectorTest();

        doubleRecordCheckRequests(updateServiceDoubleRecordCheckConnectorTest);
    }

    private static void doubleRecordCheckRequests(UpdateServiceDoubleRecordCheckConnectorTest connectorTest)
            throws UpdateServiceDoubleRecordCheckConnectorException {

    }

}
