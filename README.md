# Updateservice Rest Connector
Jar library containing helper functions for calling the rest end points on updateservice.

### Usage
In pom.xml add this dependency:

    <groupId>dk.dbc</groupId>
    <artifactId>updateservice-rest-connector</artifactId>
    <version>1.0-SNAPSHOT</version>

In your EJB add the following inject:

    @Inject
    private UpdateServiceDoubleRecordCheckConnector doubleRecordCheckConnector;

You must have the following environment variables in your deployment:

    UPDATE_SERVICE_URL

### Examples
    BibliographicRecord bibliographicRecord // ... fill out data
    UpdateRecordResult result = doubleRecordCheckConnector.doubleRecordCheck(bibliographicRecord);
    if (result.getUpdateStatus() != UpdateStatusEnum.OK) {
        // Handler double record situation
    } 
