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

import java.util.List;

import eu.clarin.fcs.tester.FCSTest;
import eu.clarin.fcs.tester.FCSTestCase;
import eu.clarin.fcs.tester.FCSTestContext;
import eu.clarin.fcs.tester.FCSTestProfile;
import eu.clarin.fcs.tester.FCSTestHandler;
import eu.clarin.fcs.tester.FCSTestResult;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUSimpleClient;

@FCSTestCase(priority=2010, profiles = {
        FCSTestProfile.CLARIN_FCS_LEGACY
})
public class TestScan2 extends FCSTest {

    @Override
    public String getName() {
        return "Scan";
    }


    @Override
    public String getDescription() {
        return "Scan on 'fcs.resource = root'";
    }


    @Override
    public String getExpected() {
        return "One or more terms returned within scan response";
    }


    @Override
    public FCSTestResult perform(FCSTestContext context, SRUSimpleClient client,
            FCSTestHandler handler) throws SRUClientException {
        SRUScanRequest req = context.createScanRequest();
        req.setScanClause("fcs.resource=root");
        client.scan(req, handler);

        if (handler.findDiagnostic("info:srw/diagnostic/1/4")) {
            return makeWarning("Endpoint does not support 'scan' operation");
        } else {
            if (handler.getDiagnosticCount() == 0) {
                List<String> terms = handler.getTerms();
                if (terms.size() >= 1) {
                    return makeSuccess();
                } else {
                    return makeWarning("Scan on 'fcs.resource = root' should yield at least one collection");
                }
            } else {
                return makeWarningUnexpectedDiagnostics();
            }
        }
    }

}
