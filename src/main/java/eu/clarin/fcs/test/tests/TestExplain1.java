package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUExplainRequest;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUVersion;

@SRUTestCase(priority=1000)
public class TestExplain1 implements SRUTest {

    @Override
    public String getName() {
        return "Explain";
    }


    @Override
    public String getDescription() {
        return "Regular explain request using SRU version 1.2";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUExplainRequest req = new SRUExplainRequest(baseURI);
        req.setVersion(SRUVersion.VERSION_1_2);
        client.explain(req, handler);
        return SUCCESS;
    }

}
