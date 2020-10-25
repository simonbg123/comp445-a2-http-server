package test;

import http.HttpRequest;
import http.HttpRequestHandler;
import http.HttpResponse;

public class MockHttpRequestHandler implements HttpRequestHandler {
    @Override
    public HttpResponse handleRequest(HttpRequest httpRequest) {


        String responseBody = "We received your request. Thank you!: \n" + httpRequest.getEntityBody();

        return new HttpResponse.Builder(HttpResponse.version_1_0)
                .date("We're today.")
                .statusCodeAndReasonPhrase(HttpResponse.ok200)
                .contentLength(responseBody.getBytes().length)
                .entityBody(responseBody)
                .build();
    }
}
