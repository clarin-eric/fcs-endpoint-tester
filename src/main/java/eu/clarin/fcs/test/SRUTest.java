package eu.clarin.fcs.test;

import eu.clarin.fcs.test.SRUTest.Result.Code;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUClientException;


public interface SRUTest {
    public static class Result {
        public static enum Code {
            SUCCESS, WARNING, ERROR
        }

        private Code code;
        private String message;


        public Result(Code code, String message) {
            this.code = code;
            this.message = message;
        }


        public Result(Code code) {
            this(code, null);
        }


        public Code getCode() {
            return code;
        }


        public String getMessage() {
            return message;
        }
    } // inner class Result
    public static final Result SUCCESS = new Result(Code.SUCCESS);
    public static final Result WARNING_DIAGNOSTICS = new Result(Code.WARNING,
            "One or more unexpected diagnostic reported by endpoint");
    public static final Result ERROR = new Result(Code.ERROR,
            "Something went wrong");
    public static final Result ERROR_NO_DIAGNOSTIC = new Result(Code.ERROR,
            "Endpoint did not report expected diagnostic");


    public String getName();


    public String getDescription();


    public Result performTest(SRUSimpleClient client, SRUTestingHandler handler,
            String baseURI, String searchTerm) throws SRUClientException;

} // class SRUEndpintTest
