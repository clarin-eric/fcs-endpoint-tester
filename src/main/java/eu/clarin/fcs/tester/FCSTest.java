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
package eu.clarin.fcs.tester;

import java.util.Collections;
import java.util.List;
import java.util.logging.LogRecord;

import eu.clarin.sru.client.SRUClient;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUDiagnostic;
import eu.clarin.sru.client.SRUExplainResponse;
import eu.clarin.sru.client.SRUScanResponse;
import eu.clarin.sru.client.SRUSearchRetrieveResponse;
import eu.clarin.sru.client.SRUTerm;


public abstract class FCSTest {
    public abstract String getName();


    public abstract String getDescription();


    public abstract String getExpected();


    public abstract FCSTestResult perform(FCSTestContext context,
            SRUClient client) throws SRUClientException;


    protected String escapeCQL(String q) {
        if (q.contains(" ")) {
            return "\"" + q + "\"";
        } else {
            return q;
        }
    }

    
    protected String escapeFCS(String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (int i = 0; i < q.length(); i++) {
            final char ch = q.charAt(i);
            switch (ch) {
            case '\\':
                sb.append("\\\\");
                break;
            case '\'':
                sb.append("\\'");
                break;
            default:
                sb.append(ch);
            }
        }
        sb.append("\"");
        return sb.toString();
    }


    protected boolean findDiagnostic(SRUExplainResponse res, String uri) {
        return dofindDiagnostic(res.getDiagnostics(), uri);

    }


    protected boolean findDiagnostic(SRUScanResponse res, String uri) {
        return dofindDiagnostic(res.getDiagnostics(), uri);

    }


    protected boolean findDiagnostic(SRUSearchRetrieveResponse res,
            String uri) {
        return dofindDiagnostic(res.getDiagnostics(), uri);

    }


    private boolean dofindDiagnostic(List<SRUDiagnostic> diagnostics,
            String uri) {
        if (diagnostics != null) {
            for (SRUDiagnostic diagnostic : diagnostics) {
                if (uri.equals(diagnostic.getURI())) {
                    return true;
                }
            }
        }
        return false;
    }


//    protected int getDiagnosticCount(SRUExplainResponse res) {
//        return doGetDiagnosticCount(res.getDiagnostics());
//    }
//
//
//    protected int getDiagnosticCount(SRUScanResponse res) {
//        return doGetDiagnosticCount(res.getDiagnostics());
//    }
//
//
//    protected int getDiagnosticCount(SRUSearchRetrieveResponse res) {
//        return doGetDiagnosticCount(res.getDiagnostics());
//    }
//
//
//    private int doGetDiagnosticCount(List<SRUDiagnostic> diagnostics) {
//        return diagnostics != null ? diagnostics.size() : 0;
//    }

    protected int getTermsCount(SRUScanResponse res) {
        return res.getTerms() != null ? res.getTerms().size() : 1;
    }

    protected List<SRUTerm> getTerms(SRUScanResponse res) {
        if (res.getTerms() != null) {
            return res.getTerms();
        } else {
           return Collections.emptyList(); 
        }
    }


    protected boolean isDiagnostic(SRUDiagnostic d, String uri) {
        return uri.equals(d.getURI());
    }


    protected FCSTestResult makeSuccess() {
        return new FCSTestResult(this, FCSTestResult.Code.SUCCESS,
                "The test case was processed successfully");
    }


    protected FCSTestResult makeSkipped() {
        return new FCSTestResult(this, FCSTestResult.Code.SKIPPED,
                "The test case was skipped because it was not applicable");
    }


    protected FCSTestResult makeWarning(String message) {
        return new FCSTestResult(this, FCSTestResult.Code.WARNING, message);
    }


    protected FCSTestResult makeWarningUnexpectedDiagnostics() {
        return new FCSTestResult(this, FCSTestResult.Code.WARNING,
                "One or more unexpected diagnostic reported by endpoint");
    }


    protected FCSTestResult makeGenericError() {
        return new FCSTestResult(this, FCSTestResult.Code.ERROR,
                "Something went wrong");
    }


    protected FCSTestResult makeError(String message) {
        return new FCSTestResult(this, FCSTestResult.Code.ERROR, message);
    }


    protected FCSTestResult makeError(List<LogRecord> records) {
        return new FCSTestResult(this, FCSTestResult.Code.ERROR,
                "An error occurred while processing the test case", records);
    }


    protected FCSTestResult makeErrorNoDiagnostic(String diagnostic) {
        return new FCSTestResult(this, FCSTestResult.Code.ERROR, String.format(
                "Endpoint did not report expected diagnostic: %s", diagnostic));
    }

} // class FCSTest
