package http;

public class HttpResponse {

    public static final String ok200 = "200 OK";
    public static final String created201 = "201 Created";
    public static final String version_1_0 = "HTTP/1.0";
    public static final String badRequest400 = "400 Bad Request";
    public static final String notFound404 = "404 Not Found";

    public static final String internalServerError500 = "500 Internal Server Error";

    public static final String contentTypePlainText = "Content-Type: text/plain";

    /**
     *  Status line
     */
    private String httpVersion;
    private String statusCodeAndReasonPhrase;
    private String date; // optional
    private int contentLength; // optional. If no body, then must be defined as 0.
    private String contentType; // optional, text/plain

    /**
     * Entity Body
     */
    private String entityBody; //when applicable


    private HttpResponse(Builder builder) {
        this.httpVersion = builder.httpVersion;
        this.statusCodeAndReasonPhrase = builder.statusCodeAndReasonPhrase;
        this.date = builder.date;
        this.contentLength = builder.contentLength;
        this.contentType = builder.contentType;
        this.entityBody = builder.entityBody;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getStatusCodeAndReasonPhrase() {
        return statusCodeAndReasonPhrase;
    }

    public String getDate() {
        return date;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEntityBody() {
        return entityBody;
    }

    public static class Builder {
        private String httpVersion;
        private String statusCodeAndReasonPhrase;
        private String date;
        private int contentLength;
        private String contentType;
        private String entityBody;

        public Builder(String version) {
            this.httpVersion = version;
        }

        public Builder statusCodeAndReasonPhrase(String statusAndReason) {
            this.statusCodeAndReasonPhrase = statusAndReason;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder contentLength(int length) {
            this.contentLength = length;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder entityBody(String entityBody) {
            this.entityBody = entityBody;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }
}
