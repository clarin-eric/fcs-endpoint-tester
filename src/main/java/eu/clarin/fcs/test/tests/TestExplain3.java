package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUExplainRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=1020)
public class TestExplain3 implements SRUTest {

    @Override
    public String getName() {
        return "Explain";
    }


    @Override
    public String getDescription() {
        return "Explain without 'version' argument";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUExplainRequest req = new SRUExplainRequest(baseURI);
        req.setExtraRequestData(SRUExplainRequest.X_MALFORMED_VERSION,
                SRUExplainRequest.MALFORMED_OMIT);
        client.explain(req, handler);
        return SUCCESS;
    }

}
