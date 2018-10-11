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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClient;
import eu.clarin.sru.client.SRUClientConfig;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUExplainRequest;
import eu.clarin.sru.client.SRUExplainResponse;
import eu.clarin.sru.client.SRUVersion;
import eu.clarin.sru.client.fcs.ClarinFCSClientBuilder;
import eu.clarin.sru.client.fcs.ClarinFCSConstants;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescriptionParser;


public class FCSEndpointTester implements ServletContextListener {
    public interface ProgressListener {
        public void updateProgress(String message);

        public void updateMaximum(int maximum);

        public void onDone(FCSTestContext context, List<FCSTestResult> results);

        public void onError(String message, Throwable e);
    }
    private static final String TESTCASE_PACKAGE =
            "eu.clarin.fcs.tester.tests";
    private static final String USER_AGENT = "FCSEndpointTester/1.0.0";
    private static final int DEFAULT_CONNECT_TIMEOUT = -1;
    private static final int DEFAULT_SOCKET_TIMEOUT  = -1;
    private static final List<FCSTest> TESTS;
    private static final Logger logger =
            LoggerFactory.getLogger(FCSEndpointTester.class);
    private final FCSLoggingHandler logcapturehandler =
            new FCSLoggingHandler();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final FCSEndpointTester INSTANCE = new FCSEndpointTester();


    public static FCSEndpointTester getInstance() {
        return INSTANCE;
    }


    private void destroy() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            logger.debug("thread pool terminated");
        } catch (InterruptedException e) {
            /* IGNORE */
        }
    }


    private static CloseableHttpClient createHttpClient(int connectTimeout,
            int socketTimeout) {
        final PoolingHttpClientConnectionManager manager =
                new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(8);
        manager.setMaxTotal(128);

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoReuseAddress(true)
                .setSoLinger(0)
                .build();

        final RequestConfig requestConfig = RequestConfig.custom()
                .setAuthenticationEnabled(false)
                .setRedirectsEnabled(true)
                .setMaxRedirects(4)
                .setCircularRedirectsAllowed(false)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(0) /* infinite */
                .build();

        return HttpClients.custom()
                .setUserAgent(USER_AGENT)
                .setConnectionManager(manager)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .build();
    }


    public void performTests(final ProgressListener listener,
            final FCSTestProfile profile,
            final String endpointURI,
            final String searchTerm,
            final boolean strictMode,
            final boolean performProbeQuest,
            final int connectTimeout,
            final int socketTimeout) {
        if (listener == null) {
            throw new NullPointerException("listener == null");
        }
        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        FCSTestContext context = null;
                        if (performProbeQuest) {
                            doPerformProbeRequest(listener, endpointURI);
                        }
                        // auto-detect FCS version?
                        if (profile == null) {
                            context = doPerformAutodetect(listener,
                                    endpointURI,
                                    searchTerm,
                                    strictMode,
                                    connectTimeout,
                                    socketTimeout);
                        } else {
                            context = new FCSTestContext(profile,
                                    endpointURI,
                                    searchTerm,
                                    strictMode,
                                    connectTimeout,
                                    socketTimeout);
                        }
                        context.init();
                        doPerformTests(listener, context);
                    } catch (IOException e) {
                        listener.onError("An error occurred", e);
                    } catch (SRUClientException e) {
                        listener.onError(e.getMessage(), e.getCause());
                    } catch (Throwable t) {
                        logger.error("Internal error!", t);
                        listener.onError("Internal error!", t);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            listener.onError("Error starting tests", null);
        }
    }


    private FCSTestContext doPerformAutodetect(final ProgressListener listener,
            final String endpointURI,
            final String searchTerm,
            final boolean strictMode,
            final int connectTimeout,
            final int socketTimeout) throws SRUClientException {
        listener.updateProgress("Detecting CLARIN-FCS profile ...");

        FCSTestProfile profile = null;

        SRUClient client = new ClarinFCSClientBuilder()
                .addDefaultDataViewParsers()
                .setDefaultSRUVersion(SRUVersion.VERSION_2_0)
                .unknownDataViewAsString()
                .enableLegacySupport()
                .registerExtraResponseDataParser(
                        new ClarinFCSEndpointDescriptionParser())
                .buildClient();

        try {
            SRUExplainRequest request = new SRUExplainRequest(endpointURI);
            request.setStrictMode(false);
            request.setVersion(SRUVersion.VERSION_1_2);
            request.setExtraRequestData(ClarinFCSConstants.X_FCS_ENDPOINT_DESCRIPTION,
                    ClarinFCSConstants.TRUE);
            request.setParseRecordDataEnabled(true);
            SRUExplainResponse response = client.explain(request);

            ClarinFCSEndpointDescription ed =
                    response.getFirstExtraResponseData(ClarinFCSEndpointDescription.class);
            if (ed != null) {
                if (ed.getVersion() == 1) {
                    profile = FCSTestProfile.CLARIN_FCS_1_0;
                }
            } else {
                logger.debug("assume legacy");
                profile = FCSTestProfile.CLARIN_FCS_LEGACY;
            }

            if (profile == null) {
                request = new SRUExplainRequest(endpointURI);
                request.setStrictMode(false);
                request.setVersion(SRUVersion.VERSION_2_0);
                request.setExtraRequestData(
                        ClarinFCSConstants.X_FCS_ENDPOINT_DESCRIPTION,
                        ClarinFCSConstants.TRUE);
                request.setParseRecordDataEnabled(true);
                try {
                    response = client.explain(request);

                    ed = response.getFirstExtraResponseData(
                            ClarinFCSEndpointDescription.class);
                    if (ed != null) {
                        if (ed.getVersion() == 2) {
                            profile = FCSTestProfile.CLARIN_FCS_2_0;
                        }
                    }
                } catch (SRUClientException e) {
                    if ((e.getMessage() != null) && (e.getMessage()
                            .contains("responded with different version"))) {
                        throw new SRUClientException(
                                "Seriously broken Endpoint: when trying to " +
                                "detect FCS 2.0 the Endpoint illegally " +
                                "responded with a SRU 1.2 reponse to a " +
                                "SRU 2.0 request!");
                    } else {
                        throw e;
                    }
                }
            }
            if (profile != null) {
                final FCSTestContext context =
                        new FCSTestContext(profile,
                                endpointURI,
                                searchTerm,
                                strictMode,
                                connectTimeout,
                                socketTimeout);
                return context;
            }
        } catch (SRUClientException e) {
            logger.error("error", e);
            throw new SRUClientException("An error occured while " +
                    "auto-detecting CLARIN-FCS version", e);
        }
        throw new SRUClientException("Unable to auto-detect CLARIN-FCS version!");
    }


    private void doPerformTests(final ProgressListener listener,
            final FCSTestContext context) {
        /* make sure that we'll capture the logging records */
        java.util.logging.Logger l =
                java.util.logging.Logger.getLogger("eu.clarin.sru");
        l.setLevel(Level.FINEST);
        boolean found = false;
        Handler[] handlers = l.getHandlers();
        if (handlers != null) {
            for (Handler handler : handlers) {
                if (handler == logcapturehandler) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            l.addHandler(logcapturehandler);
        }


        List<FCSTest> tests = new ArrayList<FCSTest>();
        for (FCSTest test : TESTS) {
            final FCSTestCase tc =
                    test.getClass().getAnnotation(FCSTestCase.class);
            if (Arrays.binarySearch(tc.profiles(), context.getProfile()) >= 0) {
                tests.add(test);
            }
        }

        List<FCSTestResult> results = null;

        final SRUClient client = context.getClient();
        final int totalCount = tests.size();
        int num = 1;
        listener.updateMaximum(totalCount);
        for (FCSTest test : tests) {
            if (results == null) {
                results = new LinkedList<FCSTestResult>();
            }
            logger.debug("running test {}:{}", num, test.getName());
            final String message = String.format(
                    "Performing test \"%s:%s\" (%d/%d) ...",
                    context.getProfile().toDisplayString(),
                    test.getName(), num, totalCount);
            listener.updateProgress(message);
            num++;

            FCSTestResult result = null;
            try {
                logcapturehandler.publish(new LogRecord(Level.FINE,
                        "running test class " + test.getClass().getName()));
                result = test.perform(context, client);
                result.setLogRecords(logcapturehandler.getLogRecords());
            } catch (SRUClientException e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = "unclassified exception from SRUClient";
                }
                final LogRecord record = new LogRecord(Level.SEVERE, msg);
                record.setThrown(e);
                logcapturehandler.publish(record);
                result = new FCSTestResult(test,
                        FCSTestResult.Code.ERROR,
                        msg,
                        logcapturehandler.getLogRecords());
            } catch (Throwable t) {
                final LogRecord record = new LogRecord(Level.SEVERE,
                        "The endpoint tester as triggered an internal error! " +
                        "Please report to developers.");
                logcapturehandler.publish(record);
                result = new FCSTestResult(test,
                        FCSTestResult.Code.ERROR,
                        record.getMessage(),
                        logcapturehandler.getLogRecords());
                logger.error("an internal error occured", t);
            }
            results.add(result);
        } // for
        listener.onDone(context, results);
    }


    private void doPerformProbeRequest(final ProgressListener listener,
            final String baseURI) throws IOException {
        try {
            logger.debug("performing initial probe request to {}",
                    baseURI);
            listener.updateProgress(
                    "Performing HTTP HEAD probe request ...");
            final CloseableHttpClient client =
                    createHttpClient(DEFAULT_CONNECT_TIMEOUT,
                            DEFAULT_SOCKET_TIMEOUT);
            final HttpHead request = new HttpHead(baseURI);
            HttpResponse response = null;
            try {
                response = client.execute(request);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("Probe request to endpoint " +
                            "returned unexpected HTTP status " +
                            status.getStatusCode());
                }
            } finally {
                HttpClientUtils.closeQuietly(response);

                HttpClientUtils.closeQuietly(client);
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e);
        }
    }


    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("initialized");
    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.info("shutting down ...");
        INSTANCE.destroy();
    }


    static {
        List<FCSTest> tests = null;
        try {
            Reflections reflections = new Reflections(TESTCASE_PACKAGE);
            Set<Class<?>> annotations =
                    reflections.getTypesAnnotatedWith(FCSTestCase.class);
            if ((annotations != null) && !annotations.isEmpty()) {
                List<Class<?>> classes = new ArrayList<Class<?>>(
                        annotations.size());
                for (Class<?> clazz : annotations) {
                    FCSTestCase tc = clazz.getAnnotation(FCSTestCase.class);
                    if ((tc != null) && (tc.priority() >= 0)) {
                        classes.add(clazz);
                    }
                }
                Collections.sort(classes, new Comparator<Class<?>>() {
                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        FCSTestCase a1 = o1.getAnnotation(FCSTestCase.class);
                        FCSTestCase a2 = o2.getAnnotation(FCSTestCase.class);
                        return a1.priority() - a2.priority();
                    }
                });
                for (Class<?> clazz : classes) {
                    if (tests == null) {
                        tests = new ArrayList<FCSTest>(classes.size());
                    }
                    tests.add((FCSTest) clazz.newInstance());
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (tests != null) {
            TESTS = Collections.unmodifiableList(tests);
        } else {
            TESTS = Collections.emptyList();
        }
    }

} // class SRUEndpointTester
