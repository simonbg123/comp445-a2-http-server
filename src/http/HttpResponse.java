package http;

public class HttpResponse {

    public static final String ok200 = "200 OK";
    public static final String created201 = "201 Created";

    public static final String badRequest400 = "400 Bad Request";
    public static final String notFound404 = "404 Not Found";

    public static final String internalServerError500 = "500 Internal Server Error";

    public static final String contentTypePlainText = "Content-Type: text/plain";

    /**
     *  Status line
     */
    String httpVersion;
    String statusCodeAndReasonPhrase;
    String date; // optional
    String contentLength; // optional. If no body, then must be defined as 0.
    String contentType; // optional, text/plain

    /**
     * Entity Body
     */
    String entityBody; //when applicable
}
