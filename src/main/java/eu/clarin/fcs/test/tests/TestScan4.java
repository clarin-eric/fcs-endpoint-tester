package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=2040)
public class TestScan4 implements SRUTest {

    @Override
    public String getName() {
        return "Scan";
    }


    @Override
    public String getDescription() {
        return "CLARIN-FCS: scan on 'fcs.resource = root' with bad 'maximumTerms' argument (expecting diagnostic \"info:srw/diagnostic/1/6\")";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUScanRequest req = new SRUScanRequest(baseURI);
        req.setScanClause("fcs.resource=root");
        req.setExtraRequestData(SRUScanRequest.X_MALFORMED_MAXIMUM_TERMS,
                "invalid");
        client.scan(req, handler);
        return handler.findDiagnostic("info:srw/diagnostic/1/6") ? SUCCESS : ERROR_NO_DIAGNOSTIC;
    }

}
