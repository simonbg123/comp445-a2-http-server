package main;

import http.HttpRequest;
import http.HttpRequestHandler;
import http.HttpResponse;
import http.HttpServer;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static http.HttpServer.VERSION_1_0;

public class HttpFileServer implements HttpRequestHandler {

    private String rootDir;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

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

        HttpResponse httpResponse = null;
        String path = rootDir + httpRequest.getRequestURI();
        File file = new File(path);

        if (!file.isFile()){
            return HttpServer.getErrorResponse(HttpResponse.NOT_FOUND_404);
        }
        else if (!file.canRead()) {
            String message = "Cannot read the specified file: " + path;
            return HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
        }

        //if ok, get the string and make a 200ok response with body
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            StringBuilder sb = new StringBuilder();
            String line;
            for (line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line + '\n');
            }

            httpResponse = getResponse(HttpResponse.OK_200, sb.toString());

        }
        catch (FileNotFoundException fnf) {
            String message = "Couldn't find resource: " + path;
            httpResponse = HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
        }
        catch (IOException e) {
            String message = "Problem reading the specified file: " + path;
            httpResponse = HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
        }

        return httpResponse;
    }

    private HttpResponse handlePOST(HttpRequest httpRequest) {
        //try to create the resource
        //if there is text, write the text to the file
        //handle exception with a 500 error

        //return appropriate message "created succesfully, etc."
        return null;
    }

    private HttpResponse getResponse(String statusAndReason, String message) {

        int contentLength = message.getBytes().length;

        return new HttpResponse.Builder(VERSION_1_0)
                .statusCodeAndReasonPhrase(statusAndReason)
                .date(formatter.format(ZonedDateTime.now()))
                .contentLength(contentLength)
                .entityBody(message)
                .build();
    }

    private HttpResponse getResponse(String statusAndReason) {

        return getResponse(statusAndReason, "");
    }
}
