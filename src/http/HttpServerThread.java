package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class HttpServerThread extends Thread {

    private Socket clientSocket;
    private HttpRequestHandler requestHandler;
    boolean verbose;
    Object verboseOutputLock = null;

    public HttpServerThread(Socket clientSocket, HttpRequestHandler requestHandler, boolean verbose, Object verboseOutputLock) {
        this.clientSocket = clientSocket;
        this.requestHandler = requestHandler;
        this.verbose = verbose;
        this.verboseOutputLock = verboseOutputLock;
    }

    public HttpServerThread(Socket clientSocket, HttpRequestHandler requestHandler, boolean verbose) {
        this(clientSocket, requestHandler, verbose, null);
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()))) {

            if (verbose) threadSafeOutput("Server contacted by " + clientSocket.getInetAddress() + "\n");

            HttpRequest httpRequest = null;
            HttpResponse httpResponse = null;
            try {
                httpRequest = HttpServer.extractRequest(in);
                if (verbose) threadSafeOutput("Request:\n" + httpRequest + "\n");
                httpResponse = requestHandler.handleRequest(httpRequest);
                if (verbose) threadSafeOutput("Response:\n" + httpResponse + "\n");
            }
            catch (HeaderIOException e) {
                httpResponse = HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, e.getMessage());
            }
            catch (HttpRequestFormatException e) {
                httpResponse = HttpServer.getErrorResponse(HttpResponse.BAD_REQUEST_400, e.getMessage());
            }
            catch (HttpRequestUnsupportedVersionException e) {
                httpResponse = HttpServer.getErrorResponse(HttpResponse.UNSPPORTED_VERSION_505, e.getMessage());
            }

            out.print(httpResponse.toString());
            out.flush();
        }
        catch (Exception e) {
            if(verbose) {
                threadSafeOutput("\n" + HttpServer.CLIENT_SOCKET_PROBLEM + " :\n" + e.getMessage() + "\n");
            }
        }
    }

    public void threadSafeOutput(String message) {
        synchronized (verboseOutputLock) {
            System.out.println(message);
        }
    }
}
