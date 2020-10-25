package main;

import http.HttpRequest;
import http.HttpRequestHandler;
import http.HttpResponse;

public class HttpFileServer implements HttpRequestHandler {

    private String rootDir;

    public HttpFileServer(String rootDir) {
        this.rootDir = rootDir;
    }

    public HttpFileServer() {
        this.rootDir = System.getProperty("user.dir");
    }

    @Override
    public HttpResponse handleRequest(HttpRequest httpRequest) {

        HttpResponse httpResponse = null;
        if (httpRequest.getMethod().equalsIgnoreCase(HttpRequest.GET)) {
            httpResponse = handleGET(httpRequest);
        }
        else if (httpRequest.getMethod().equalsIgnoreCase(HttpRequest.POST)) {
            httpResponse = handlePOST(httpRequest);
        }

        return httpResponse;
    }

    private HttpResponse handleGET(HttpRequest httpRequest) {
        //check if file exists and is readable
        // if not return an error response - use HttpServer method
        //500 if not readable

        //if ok, get the string and make a 200ok response with body
        return null;
    }

    private HttpResponse handlePOST(HttpRequest httpRequest) {
        //try to create the resource
        //if there is text, write the text to the file
        //handle exception with a 500 error

        //return appropriate message "created succesfully, etc."
        return null;
    }
}
