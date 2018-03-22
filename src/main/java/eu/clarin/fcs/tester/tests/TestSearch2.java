/**
 * This software is copyright (c) 2013 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.fcs.tester.tests;

import eu.clarin.fcs.tester.FCSTest;
import eu.clarin.fcs.tester.FCSTestCase;
import eu.clarin.fcs.tester.FCSTestContext;
import eu.clarin.fcs.tester.FCSTestProfile;
import eu.clarin.fcs.tester.FCSTestHandler;
import eu.clarin.fcs.tester.FCSTestResult;
import eu.clarin.sru.client.SRUClientConstants;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;

@FCSTestCase(priority=Integer.MIN_VALUE, profiles = {
        FCSTestProfile.CLARIN_FCS_1_0,
        FCSTestProfile.CLARIN_FCS_2_0,
        FCSTestProfile.CLARIN_FCS_LEGACY
})
public class TestSearch2 extends FCSTest {

    @Override
    public String getName() {
        return "SearchRetrieve";
    }


    @Override
    public String getDescription() {
        return "Searching for random string with CLARIN FCS " +
                "record schema '" + ClarinFCSRecordData.RECORD_SCHEMA + "'";
    }


    @Override
    public String getExpected() {
        return "No errors or diagnostics";
    }


    @Override
    public FCSTestResult perform(FCSTestContext context, SRUSimpleClient client,
            FCSTestHandler handler) throws SRUClientException {
        SRUSearchRetrieveRequest req = context.createSearchRetrieveRequest();
        req.setQuery(SRUClientConstants.QUERY_TYPE_CQL,
                escapeCQL(context.getRandomSearchTerm()));
        req.setRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA);
        client.searchRetrieve(req, handler);
        return handler.getDiagnosticCount() == 0
                ? makeSuccess()
                : makeWarningUnexpectedDiagnostics();
    }

}
