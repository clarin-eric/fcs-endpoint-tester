package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=2000)
public class TestScan1 implements SRUTest {

    @Override
    public String getName() {
        return "Scan";
    }


    @Override
    public String getDescription() {
        return "Scan with missing 'scanClause' argument (expecting diagnostic \"info:srw/diagnostic/1/7\")";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUScanRequest req = new SRUScanRequest(baseURI);
        req.setExtraRequestData(SRUScanRequest.X_MALFORMED_SCAN_CLAUSE,
                SRUScanRequest.MALFORMED_OMIT);
        client.scan(req, handler);
        return handler.findDiagnostic("info:srw/diagnostic/1/7") ? SUCCESS : ERROR_NO_DIAGNOSTIC;
    }

}
