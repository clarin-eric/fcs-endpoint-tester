package eu.clarin.fcs.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


class SRULoggingHandler extends Handler {
    private final Map<Long, List<LogRecord>> messages = new HashMap<Long, List<LogRecord>>();

    @Override
    public void publish(LogRecord record) {
        Long id = Thread.currentThread().getId();

        List<LogRecord> records;
        synchronized (messages) {
            records = messages.get(id);
            if (records == null) {
                records = new ArrayList<LogRecord>();
                messages.put(id, records);
            }
        }
        records.add(record);
    }


    @Override
    public void flush() {
    }


    @Override
    public void close() throws SecurityException {
    }


    List<LogRecord> getLogRecords() {
        synchronized (messages) {
            return messages.remove(Thread.currentThread().getId());
        }
    }

} // class DiagnosticCapturingHandler
