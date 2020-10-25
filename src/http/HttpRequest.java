package http;

/**
 * Encapsulate fields of an Http Request. Uses the builder pattern
 * to construct an object of this class.
 */
public class HttpRequest {

    public static final String GET = "GET";
    public static final String POST = "POST";

    /**
     * Request Line
     */
    private String method;
    private String requestURI;
    private String httpVersion;
    private int contentLength; // if and only if there is a body. POST must have a content-length, even if it is 0
    private String entityBody;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.requestURI = builder.requestURI;
        this.httpVersion = builder.httpVersion;
        this.contentLength = builder.contentLength; // if and only if there is a body
        this.entityBody = builder.entityBody;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getEntityBody() {
        return entityBody;
    }

    public static class Builder {
        String method;
        String requestURI;
        String httpVersion;
        int contentLength; // if and only if there is a body
        String entityBody;

        public Builder(String method) {
            this.method = method;
        }

        public Builder requestURI(String URI) {
            this.requestURI = URI;
            return this;
        }

        public Builder httpVersion(String version) {
            this.httpVersion = version;
            return this;
        }

        public Builder contentLength(int length) {
            this.contentLength = length;
            return this;
        }

        public Builder entityBody(String body) {
            this.entityBody = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
