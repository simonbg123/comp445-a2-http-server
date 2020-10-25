package test;

import http.HttpRequest;
import http.HttpRequestHandler;
import http.HttpResponse;
import http.HttpServer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MockHttpRequestHandler implements HttpRequestHandler {
    @Override
    public HttpResponse handleRequest(HttpRequest httpRequest) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

        String responseBody = "We received your request. Thank you!: \n" + httpRequest.getEntityBody();

        return new HttpResponse.Builder(HttpServer.VERSION_1_0)
                .date(formatter.format(ZonedDateTime.now()))
                .statusCodeAndReasonPhrase(HttpResponse.OK_200)
                .contentLength(responseBody.getBytes().length)
                .entityBody(responseBody)
                .build();
    }
}
