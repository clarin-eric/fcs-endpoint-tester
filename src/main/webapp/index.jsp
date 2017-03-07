<%@page import="javax.naming.Context"%>
<%@ page language="java" pageEncoding="UTF-8" %><%
String uri = request.getParameter("uri");
if (uri == null) {
    uri = "";
}
String searchTerm = request.getParameter("searchTerm");
if (searchTerm == null) {
    searchTerm = "";
}
String location = request.getHeader("X-SERVLET-LOCATION");
if (location == null) {
    location = request.getContextPath();
}
if (!request.getRequestURI().endsWith("/")) {
    StringBuilder sb = new StringBuilder(location);
    if (!location.endsWith("/")) {
        sb.append("/");
    }
    response.sendRedirect(sb.toString());
} else {
    StringBuilder testUri = new StringBuilder(location);
    if (!location.endsWith("/")) {
        testUri.append("/");
    }
    testUri.append("EndpointTester");
%><html>
    <head>
        <title>CLARIN FCS SRU/CQL Conformance Test</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="tester.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <h1>CLARIN FCS SRU/CQL Conformance Test</h1>
        <form action="<%= testUri %>" method="get">
            <label for="uri">Endpoint BaseURI</label>
            <input id="uri" type="text" name="uri" value="<%= uri %>" size="64" maxlength="255"/>
            <input id="cb_strict" type="checkbox" name="strict" value="strict" checked="checked" />
            <label for="cb_strict">Strict Mode</label>
            <input id="submit" type="submit" name="doit" value="Go!" /> <br />
            <label for="searchTerm">Search term (e.g. a word)</label>
            <input id="searchTerm" type="text" name="searchTerm" value="<%= searchTerm %>" size="32" maxlength="64" /> <br />
            <small>Note: the <em>search term</em> must occur at least one in your data. It is used to check, if searchRetrieve is properly implemented. Please only use a single word.</small>
        </form>
        <hr />
        <h2>SRU/CQL Resources</h2>
        <ul>
            <li><a href="http://www.loc.gov/standards/sru/index.html">SRU Specification</a></li>
            <li><a href="http://www.loc.gov/standards/sru/specs/explain.html">SRU Specification &ndash; Explain</a></li>
            <li><a href="http://www.loc.gov/standards/sru/specs/scan.html">SRU Specification &ndash; Scan</a></li>
            <li><a href="http://www.loc.gov/standards/sru/specs/search-retrieve.html">SRU Specification &ndash; Search Retrieve</a></li>
            <li><a href="http://www.loc.gov/standards/sru/resources/diagnostics-list.html">SRU Specification &ndash; Diagnostics List</a></li>
            <li><a href="http://www.loc.gov/standards/sru/sru1-1archive/xml-files/srw-types.xsd">SRU/SRW XSD (SRU Version 1.1)</a></li>
            <li><a href="http://www.loc.gov/standards/sru/sru1-1archive/xml-files/diagnostics.xsd">SRU/SRW Diagnostic XSD</a></li>
        </ul>
    </body>
</html><% } %>
