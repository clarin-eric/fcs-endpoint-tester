package eu.clarin.fcs.test.tests;

import eu.clarin.fcs.test.SRUTest;
import eu.clarin.fcs.test.SRUTestCase;
import eu.clarin.fcs.test.SRUTestingHandler;
import eu.clarin.fcs.test.SRUTestingHandler.SurrogateDiagnostic;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUDiagnostic;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUSurrogateRecordData;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;
import eu.clarin.sru.client.fcs.DataView;
import eu.clarin.sru.client.fcs.DataViewKWIC;
import eu.clarin.sru.client.fcs.Resource;
import eu.clarin.sru.client.fcs.Resource.ResourceFragment;

@SRUTestCase(priority=3500)
public class TestSearch11 implements SRUTest {
    private static final String FCS_RECORD_SCHEMA = "http://clarin.eu/fcs/1.0";
    @Override
    public String getName() {
        return "SearchRetrieve";
    }


    @Override
    public String getDescription() {
        return "CLARIN-FCS: Search for search term (expecting at least one result in CLARIN-FCS format)";
    }


    @Override
    public Result performTest(SRUSimpleClient client,
            SRUTestingHandler handler, String baseURI, String searchTerm)
            throws SRUClientException {
        SRUSearchRetrieveRequest req = new SRUSearchRetrieveRequest(baseURI);
        if (searchTerm.indexOf(' ') != -1) {
            searchTerm = '"' + searchTerm + '"';
        }
        req.setQuery(searchTerm);
        req.setRecordSchema(FCS_RECORD_SCHEMA);
        req.setMaximumRecords(5);
        client.searchRetrieve(req, handler);
        if (handler.findDiagnostic("info:srw/diagnostic/1/66")) {
            return new Result(Result.Code.ERROR, "Endpoint claims to not " +
                    "support FCS record schema (" + FCS_RECORD_SCHEMA + ")");
        } else if (handler.getRecordCount() == 0) {
            return new Result(Result.Code.WARNING, "Endpoint has no results " +"" +
                    "for search term \"" + searchTerm +
                    "\". Please supply a differnt search term.");
        } else {
            for (SRUTestingHandler.Record record : handler.getRecords()) {
                String recordSchema = record.getData().getRecordSchema();
                if (SRUSurrogateRecordData.RECORD_SCHEMA.equals(recordSchema)) {
                    final SurrogateDiagnostic data =
                            (SurrogateDiagnostic) record.getData();
                    final SRUDiagnostic d = data.getDiagnostic();

                    if (data.isDiagnostic("info:srw/diagnostic/1/67")) {
                        return new Result(Result.Code.ERROR,
                                "Endpoint cannot render record in CLARIN-FCS " +
                                "record format and returned surrogate " +
                                "diagnostic \"info:srw/diagnostic/1/67\" " +
                                "instead.");
                    } else if (data.isDiagnostic("info:clarin/sru/diagnostic/2")) {
                        return new Result(Result.Code.ERROR,
                                "Endpoint sent record with record " +
                                        "schema of '" + d.getDetails() +
                                        "' instead of '" + FCS_RECORD_SCHEMA +
                                        "' which is " +
                                        "required by CLARIN-FCS.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Endpoint returned unexpected surrgogate ")
                                .append("diagnostic \"")
                                .append(d.getURI()).append("\"");
                        if (d.getDetails() != null) {
                            sb.append("; details = \"")
                                .append(d.getDetails()).append("\"");
                        }
                        if (d.getMessage() != null) {
                            sb.append("; message = \"")
                                .append(d.getMessage()).append("\"");
                        }
                        return new Result(Result.Code.ERROR, sb.toString());
                    }
                } else if (FCS_RECORD_SCHEMA.equals(recordSchema)) {
                    final ClarinFCSRecordData data =
                            (ClarinFCSRecordData) record.getData();
                    final Resource resource = data.getResource();
                    boolean foundKwic = false;
                    if (resource.hasDataViews()) {
                        for (DataView dataView : resource.getDataViews()) {
                            if (dataView.isMimeType(DataViewKWIC.TYPE)) {
                                foundKwic = true;
                            }
                        }
                    }

                    if (resource.hasResourceFragments()) {
                        for (ResourceFragment fragment : resource.getResourceFragments()) {
                            for (DataView dataView : fragment.getDataViews()) {
                                if (dataView.isMimeType(DataViewKWIC.TYPE)) {
                                    foundKwic = true;
                                }
                            }
                        }
                    }
                    if (!foundKwic) {
                        return new Result(Result.Code.ERROR, "Endpoint did not provide mandatory KWIC dataview in results");
                    }

                } else {
                    return new Result(Result.Code.ERROR, "Endpoint does not supply results in FCS record schema (" + FCS_RECORD_SCHEMA + "\"");
                }
            }
            if (handler.getRecordCount() > req.getMaximumRecords()) {
                return new Result(Result.Code.ERROR,
                        "Endpoint did not honor upper requested limit for " +
                                "\"maximumRecords\" parameter (up to " +
                                req.getMaximumRecords() +
                                " records where requested and endpoint delivered " +
                                handler.getRecordCount() + " results)");
            }
            return SUCCESS;
        }
    }

}
