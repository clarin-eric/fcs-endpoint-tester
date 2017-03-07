package eu.clarin.fcs.test.tests;

import java.util.List;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=2030)
public class TestScan3 implements SRUTest {

    @Override
    public String getName() {
        return "Scan";
    }


    @Override
    public String getDescription() {
        return "CLARIN-FCS: scan on 'fcs.resource = root' with 'maximumTerms' with value 1";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUScanRequest req = new SRUScanRequest(baseURI);
        req.setScanClause("fcs.resource=root");
        req.setMaximumTerms(1);
        client.scan(req, handler);

        if (handler.getDiagnosticCount() > 0) {
            if (handler.findDiagnostic("info:srw/diagnostic/1/4")) {
                return new Result(Result.Code.WARNING, "Endpoint does not support 'scan' operation");
            } else {
                return WARNING_DIAGNOSTICS;
            }
        } else {
            List<String> terms = handler.getTerms();
            if (terms.size() <= 1) {
                return SUCCESS;
            } else if (terms.size() > 1) {
                return new Result(Result.Code.WARNING, "Endpoint did not honor 'maximumTerms' argument");
            }
        }
        return ERROR;
    }

}
