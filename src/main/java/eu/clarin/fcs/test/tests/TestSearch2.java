package eu.clarin.fcs.test.tests;

import org.apache.commons.lang.RandomStringUtils;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;

@SRUTestCase(priority=3010)
public class TestSearch2 implements SRUTest {
    private static final String RANDOM_SEARCH_STRING =
            RandomStringUtils.randomAlphanumeric(16);

    @Override
    public String getName() {
        return "SearchRetrieve";
    }


    @Override
    public String getDescription() {
        return "CLARIN-FCS: searching for random string '" +
                RANDOM_SEARCH_STRING + "' with CLARIN FSC record schema '" +
                ClarinFCSRecordData.RECORD_SCHEMA +
                "' (expecting no (fatal) non-surrogate diagnostics)";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUSearchRetrieveRequest req = new SRUSearchRetrieveRequest(baseURI);
        req.setQuery(RANDOM_SEARCH_STRING);
        req.setRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA);
        client.searchRetrieve(req, handler);
        return handler.getDiagnosticCount() == 0 ? SUCCESS : WARNING_DIAGNOSTICS;
    }

}
