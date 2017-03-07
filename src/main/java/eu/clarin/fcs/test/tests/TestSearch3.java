package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=3030)
public class TestSearch3 implements SRUTest {

    @Override
    public String getName() {
        return "SearchRetrieve";
    }


    @Override
    public String getDescription() {
        return "Search with missing 'query' argument (expecting diagnostic \"info:srw/diagnostic/1/7\")";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUSearchRetrieveRequest req = new SRUSearchRetrieveRequest(baseURI);
        req.setExtraRequestData(SRUSearchRetrieveRequest.X_MALFORMED_QUERY,
                SRUSearchRetrieveRequest.MALFORMED_OMIT);
        client.searchRetrieve(req, handler);
        return handler.findDiagnostic("info:srw/diagnostic/1/7") ? SUCCESS : ERROR_NO_DIAGNOSTIC;
    }

}
