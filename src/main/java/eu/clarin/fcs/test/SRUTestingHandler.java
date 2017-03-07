package eu.clarin.fcs.test;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUDefaultHandlerAdapter;
import eu.clarin.sru.client.SRUDiagnostic;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRUSurrogateRecordData;
import eu.clarin.sru.client.SRUWhereInList;


public class SRUTestingHandler extends SRUDefaultHandlerAdapter {
    public static class Record {
        private final String identifier;
        private final int position;
        private final SRURecordData data;

        public Record(String identifier, int position, SRURecordData data) {
            this.identifier = identifier;
            this.position   = position;
            this.data       = data;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getPosition() {
            return position;
        }

        public SRURecordData getData() {
            return data;
        }
    }
    public static class SurrogateDiagnostic implements SRURecordData {
        private final SRUDiagnostic diagnostic;

        public SurrogateDiagnostic(SRUDiagnostic diagnostic) {
            this.diagnostic = diagnostic;
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public String getRecordSchema() {
            return SRUSurrogateRecordData.RECORD_SCHEMA;
        }

        public SRUDiagnostic getDiagnostic() {
            return diagnostic;
        }

        public boolean isDiagnostic(String uri) {
            if (uri == null) {
                throw new NullPointerException("uri == null");
            }
            return uri.equals(diagnostic.getURI());
        }
    }
    private List<String> termList = new ArrayList<String>();
    private List<Record> recordList = new ArrayList<Record>(512);
    private List<SRUDiagnostic> diagnostics;


    @Override
    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException {
        if (this.diagnostics == null) {
            this.diagnostics = diagnostics;
        } else {
            this.diagnostics.addAll(diagnostics);
        }
    }


    @Override
    public void onTerm(String value, int numberOfRecords, String displayTerm,
            SRUWhereInList whereInList) throws SRUClientException {
        termList.add(value);
    }


    @Override
    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException {
        recordList.add(new Record(identifier, position, data));
    }


    @Override
    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException {
        recordList.add(new Record(identifier, position,
                new SurrogateDiagnostic(data)));
    }


    void reset() {
        termList.clear();
        recordList.clear();
        this.diagnostics = null;
    }


    public boolean findDiagnostic(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        if (diagnostics != null) {
            for (SRUDiagnostic diagnostic : diagnostics) {
                if (uri.equals(diagnostic.getURI())) {
                    return true;
                }
            }
        }
        return false;
    }


    public int getDiagnosticCount() {
        return diagnostics != null ? diagnostics.size() : 0;
    }


    public List<String> getTerms() {
        return termList;
    }

    public int getRecordCount() {
        return recordList.size();
    }

    public List<Record> getRecords() {
        return recordList;
    }

}
