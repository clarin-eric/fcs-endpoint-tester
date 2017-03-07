package eu.clarin.fcs.test.tests;

import java.util.List;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=2010)
public class TestScan2 implements SRUTest {

    @Override
    public String getName() {
        return "Scan";
    }


    @Override
    public String getDescription() {
        return "CLARIN-FCS: scan on fcs.resource = root";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUScanRequest req = new SRUScanRequest(baseURI);
        req.setScanClause("fcs.resource=root");
        client.scan(req, handler);

        if (handler.findDiagnostic("info:srw/diagnostic/1/4")) {
            return new Result(Result.Code.WARNING, "Endpoint does not support 'scan' operation");
        } else {
            if (handler.getDiagnosticCount() == 0) {
                List<String> terms = handler.getTerms();
                if (terms.size() >= 1) {
                    return SUCCESS;
                } else {
                    return new Result(Result.Code.WARNING, "Scan on 'fcs.resource = root' should yield at least one collection");
                }
            } else {
                return WARNING_DIAGNOSTICS;
            }
        }
    }

}
