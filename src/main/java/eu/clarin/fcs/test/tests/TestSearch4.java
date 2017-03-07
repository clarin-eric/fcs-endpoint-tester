package eu.clarin.fcs.test.tests;

import org.apache.commons.lang.RandomStringUtils;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@SRUTestCase(priority=3040)
public class TestSearch4 implements SRUTest {
    private static final String RANDOM_SEARCH_STRING =
            RandomStringUtils.randomAlphanumeric(16);

    @Override
    public String getName() {
        return "SearchRetrieve";
    }


    @Override
    public String getDescription() {
        return "Search with invalid 'recordPacking' argument (expecting diagnostic \"info:srw/diagnostic/1/71\")";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUSearchRetrieveRequest req = new SRUSearchRetrieveRequest(baseURI);
        req.setQuery(RANDOM_SEARCH_STRING);
        req.setExtraRequestData(SRUSearchRetrieveRequest.X_MALFORMED_RECORD_PACKING,
                "invalid");
        client.searchRetrieve(req, handler);
        return handler.findDiagnostic("info:srw/diagnostic/1/71") ? SUCCESS : ERROR_NO_DIAGNOSTIC;
    }

}