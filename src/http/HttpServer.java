package http;

import java.net.ServerSocket;

public class HttpServer {

    ServerSocket serverSocket;
    private HttpRequestHandler requestHandler;

    public HttpServer(HttpRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void run() {
        System.out.println("I'm a server!");
    }
}
