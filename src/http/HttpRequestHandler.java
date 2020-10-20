package http;

public interface HttpRequestHandler {

    HttpResponse handleRequest(HttpRequest httpRequest);

}
