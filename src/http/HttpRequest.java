package http;

public class HttpRequest {

    public static final String GET = "GET";
    public static final String POST = "POST";

    /**
     * Request Line
     */
    String method;
    String requestURI;
    String httpVersion;
    String contentLength; // if and only if there is a body
    String entityBody;
}
