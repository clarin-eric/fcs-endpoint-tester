package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUExplainRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=1050)
public class TestExplain5 implements SRUTest {

    @Override
    public String getName() {
        return "Explain";
    }


    @Override
    public String getDescription() {
        return "Explain with invalid value for 'version' agrument (expecting diagnostic \"info:srw/diagnostic/1/5\")";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUExplainRequest req = new SRUExplainRequest(baseURI);
        req.setExtraRequestData(SRUExplainRequest.X_MALFORMED_VERSION,
                "9.9");
        client.explain(req, handler);
        return handler.findDiagnostic("info:srw/diagnostic/1/5") ? SUCCESS : new Result(Result.Code.WARNING, "Endpoint did not report diagnostic for invalid version number");
    }

}
