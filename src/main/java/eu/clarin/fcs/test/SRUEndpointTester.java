package eu.clarin.fcs.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.reflections.Reflections;

import eu.clarin.fcs.test.SRUTest.Result;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUVersion;
import eu.clarin.sru.client.fcs.ClarinFCSRecordParser;


@SuppressWarnings("serial")
public class SRUEndpointTester extends HttpServlet {
    private static final List<SRUTest> TESTS;
    private final SRULoggingHandler logcapturehandler =
            new SRULoggingHandler();
    private DateTimeFormatter dateFmt =
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");


    @Override
    public void init() throws ServletException {
        java.util.logging.Logger l =
                java.util.logging.Logger.getLogger("eu.clarin.sru");
        l.setLevel(Level.FINEST);
        l.addHandler(logcapturehandler);
    }


    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        performTest(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        performTest(request, response);
    }


    private SRUSimpleClient createClient(boolean strict) {
        SRUSimpleClient client =
                new SRUSimpleClient(SRUVersion.VERSION_1_2, strict);
        client.registerRecordParser(new ClarinFCSRecordParser());
        return client;
    }


    private void performTest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getParameter("uri");
        if (uri != null) {
            uri = uri.trim();
        }
        if ((uri == null) || uri.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing argument: uri");
            return;
        }

        String searchTerm = request.getParameter("searchTerm");
        if (searchTerm != null) {
            searchTerm = searchTerm.trim();
        }
        if ((searchTerm == null) || searchTerm.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing argument: searchTerm");
            return;
        }

        /*
         * ok, all well. commence checks ...
         */
        String strict = request.getParameter("strict");
        if (strict != null) {
            strict = strict.trim();
            if (strict.isEmpty()) {
                strict = null;
            }
        }
        SRUSimpleClient client = createClient(strict != null);
        PrintWriter writer = null;
        try {
            URI baseURI = new URI(uri);

            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            writer = response.getWriter();
            writer.println("<html>");
            writer.println("<head><title>CLARIN FCS SRU/CQL Conformance Test</title></head>");
            writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
            writer.println("<link href=\"tester.css\" rel=\"stylesheet\" type=\"text/css\" />");
            writer.println("<script  type=\"text/javascript\" src=\"jquery-1.8.0.js\"></script>");
            writer.println("<script type=\"text/javascript\">$(document).ready(function() { $('.logview').toggle(false); });</script>");
            writer.println("<body>");
            writer.println("<h1>Results for " + baseURI + "</h1>");
            SRUTestingHandler handler = new SRUTestingHandler();
            int i = 0;
            int success = 0;
            int warning = 0;
            int error = 0;
            for (SRUTest test : TESTS) {
                Result.Code result = performSingleTest(++i, writer,
                        baseURI, searchTerm, client, handler, test);
                handler.reset();
                switch (result) {
                case SUCCESS:
                    success++;
                    break;
                case WARNING:
                    warning++;
                    break;
                case ERROR:
                    error++;
                    break;
                }
            }
            writer.println("<p>Success: " + success + ", Warning: " + warning +
                    ", Error: " + error + "</p>");
            if ((warning > 0) || (error > 0)) {
                writer.println("<p class=\"error\">Problems detected!</p>");
            }
        } catch (URISyntaxException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "illegal argument");
        } finally {
            if (writer != null) {
                writer.println("</body>");
                writer.println("</html>");
                writer.flush();
                writer.close();
            }
        }
    }


    private SRUTest.Result.Code performSingleTest(int testId,
            PrintWriter writer, URI baseURI, String searchTerm,
            SRUSimpleClient client, SRUTestingHandler handler, SRUTest test)
            throws IOException {
        writer.print("<h2 id=\"test" + testId + "\">");
        StringEscapeUtils.escapeHtml(writer, test.getName());
        writer.println("</h2>");
        if (test.getDescription() != null) {
            writer.print("<p class=\"small\">Test Case: ");
            StringEscapeUtils.escapeHtml(writer, test.getDescription());
            writer.print("</p>");
        }
        SRUTest.Result result = null;
        try {
            result = test.performTest(client, handler,
                    baseURI.toString(), searchTerm);
        } catch (SRUClientException e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = "unclassified exception from SRUClient";
            }
            LogRecord record = new LogRecord(Level.SEVERE, msg);
            record.setThrown(e);
            logcapturehandler.publish(record);
            result = new SRUTest.Result(SRUTest.Result.Code.ERROR, msg);
        } finally {
            if (result == null) {
                result = SRUTest.ERROR;
            }

            boolean hasErrors = false;
            List<LogRecord> records = logcapturehandler.getLogRecords();
            writer.print("<p>Result: ");
            switch (result.getCode()) {
            case SUCCESS:
                if (records != null) {
                    for (LogRecord record : records) {
                        if ((record.getLevel() == Level.WARNING) ||
                                (record.getLevel() == Level.SEVERE)) {
                            hasErrors = true;
                            break;
                        }
                    }
                }
                writer.println("<span class=\"success\">SUCCESS</span>");
                break;
            case WARNING:
                writer.println("<span class=\"warning\">WARNING</span>");
                break;
            case ERROR:
                writer.println("<span class=\"error\">FAIL</span>");
                break;
            }
            if (result.getMessage() != null) {
                writer.print(" ");
                writer.print(result.getMessage());
                writer.print(" ");
            }
            writer.println("<a href=\"#test" + testId + "\" class=\"togglelink\" onclick=\"$('#log" + testId + "').toggle();\">(toggle log messages)</a>");
            writer.println("</p>");

            if (hasErrors) {
                writer.println("<p class=\"error\"><em>NOTE</em>: Endpoint violates the SRU/CQL specification, but client was able to recover! See logs for details.</p>");
            }

            writer.print("<div id=\"log" + testId + "\" class=\"logview\">");
            if (records != null) {
                writer.println("<ul class=\"log\">");
                for (LogRecord record : records) {
                    String clazz = null;
                    if (record.getLevel() == Level.SEVERE) {
                        clazz = "error";
                    } else if (record.getLevel() == Level.WARNING) {
                        clazz = "warning";
                    }
                    writer.print("<li>");
                    writer.print("[");
                    writer.print(dateFmt.print(record.getMillis()));
                    writer.print("] ");
                    if (clazz != null) {
                        writer.println("<span class=\"");
                        writer.print(clazz);
                        writer.print("\">");
                    }
                    StringEscapeUtils.escapeHtml(writer, record.getMessage());
                    if (clazz != null) {
                        writer.print("</span>");
                    }
                    if (record.getThrown() != null) {
                        writer.print("<pre class=\"error\">");
                        record.getThrown().printStackTrace(writer);
                        writer.print("</pre>");
                    }
                    writer.println("</li>");
                }
                writer.println("</ul>");
            }
            writer.println("</div>");
            writer.println("<hr />");
        }

        return result != null ? result.getCode() : Result.Code.ERROR;
    }


    private static int getPriority(Class<?> clazz) {
        for (Annotation a : clazz.getDeclaredAnnotations()) {
            if (a instanceof SRUTestCase) {
                return ((SRUTestCase) a).priority();
            }
        }
        return Integer.MIN_VALUE;
    }


    static {
        Reflections reflections = new Reflections("eu.clarin.fcs.test.tests");

        List<SRUTest> tests = null;
        List<Class<?>> types = new ArrayList<Class<?>>(
                reflections.getTypesAnnotatedWith(SRUTestCase.class));
        Collections.sort(types, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return getPriority(o1) - getPriority(o2);
            }
        });

        for (Class<?> clazz : types) {
            if (tests == null) {
                tests = new ArrayList<SRUTest>();
            }
            try {
                tests.add((SRUTest) clazz.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (tests == null) {
            tests = Collections.emptyList();
        }
        TESTS = tests;
    }

} // class SRUEndpointTester
