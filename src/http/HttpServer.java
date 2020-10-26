package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class HttpServer {

    /**
     * public constants
     */
    public static final String VERSION_1_0 = "HTTP/1.0";


    private static final String CLIENT_SOCKET_PROBLEM = "Problem creating socket for client connection";
    private boolean debug; // to comply with the assignment command line option
    private int portNumber;
    private HttpRequestHandler requestHandler;

    public HttpServer(int portNumber, HttpRequestHandler requestHandler) {
        this.portNumber = portNumber;
        this.requestHandler = requestHandler;
        debug = false;
    }

    public HttpServer(int portNumber, HttpRequestHandler requestHandler, boolean debug) {
        this.portNumber = portNumber;
        this.requestHandler = requestHandler;
        this.debug = debug;
    }

    public void run() throws IOException {

        ServerSocket serverSocket = new ServerSocket(portNumber);

        if (debug) System.out.println("\nServer is running...\n");

        while(true) {

            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()))) {

                if (debug) System.out.println("Server contacted by " + clientSocket.getInetAddress() + "\n");

                HttpRequest httpRequest = null;
                HttpResponse httpResponse = null;
                try {
                    httpRequest = extractRequest(in);
                    if (debug) System.out.println("Request:\n" + httpRequest + "\n");
                    httpResponse = requestHandler.handleRequest(httpRequest);
                    if (debug) System.out.println("Response:\n" + httpResponse + "\n");
                }
                catch (HeaderIOException e) {
                    httpResponse = getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, e.getMessage());
                }
                catch (HttpRequestFormatException e) {
                    httpResponse = getErrorResponse(HttpResponse.BAD_REQUEST_400, e.getMessage());
                }

                out.print(httpResponse.toString());
                out.flush();
            }
            catch (IOException ioe) {
                //Here we can't send a 400 or 500 since the connection
                // couldn't be established. Therefore, the client application
                // will have to handle the absence of a well-formed response
                // We have to ignore the request.
            }
        }
    }

    /** * * * * * * * * * * * * * * *
     * Static public helper methods
     * * * * * * * * * * * * * * * * */

    public static HttpResponse getErrorResponse(String statusAndReason, String message) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");
        int contentLength = message.getBytes().length;

        return new HttpResponse.Builder(VERSION_1_0)
                .statusCodeAndReasonPhrase(statusAndReason)
                .date(formatter.format(ZonedDateTime.now()))
                .contentLength(contentLength)
                .entityBody(message)
                .build();
    }

    public static HttpResponse getErrorResponse(String statusAndReason) {
        return getErrorResponse(statusAndReason, "");
    }

    /** * * * * * * * * * *
     * instance methods
     * * * * * * * * * * */

    /**
     * Parses a raw HTTP request character stream  and returns the corresponding HttpRequest object.
     */
    private HttpRequest extractRequest(BufferedReader in) throws HeaderIOException, HttpRequestFormatException {

        // Get the header lines
        ArrayList<String> headerLines;
        try {
            headerLines = getHeader(in);
        }
        catch (IOException e) {
            throw new HeaderIOException("Problem extracting HTTP header");
        }

        // Parse the header lines and throw exception if ill-formed
        if (headerLines.isEmpty()) throw new HeaderIOException("Request Header is ill-formed\n");

        String[] requestLineArgs = headerLines.get(0).split("\\s+");

        if (requestLineArgs.length != 3) {
            throw new HttpRequestFormatException("Request line is ill-formed (three tokens are needed): " + headerLines.get(0) + "\n");
        }

        String method = requestLineArgs[0];
        String requestURI = requestLineArgs[1];
        String httpVersion = requestLineArgs[2];

        if (!method.equalsIgnoreCase(HttpRequest.GET) && !method.equalsIgnoreCase(HttpRequest.POST)) {
            throw new HttpRequestFormatException("Wrong method: " + method + "\n");
        }
        if (!requestURI.matches("^/.*")) {
            throw new HttpRequestFormatException("Wrong format for URI path: " + requestURI + "\n");
        }
        if (!httpVersion.equalsIgnoreCase(VERSION_1_0)) {
            throw new HttpRequestFormatException("Unsupported version: " + httpVersion + "\n");
        }

        // parse the header lines
        int contentLength = 0;
        boolean contentLengthIsSet = false;

        for (String line : headerLines) {
            if (line.trim().toLowerCase().matches("^content-length: [0-9]+$")) {
                contentLength = Integer.parseInt(line.split("\\s")[1]);
                contentLengthIsSet = true;
            }
            //Note: irrelevant header lines are ignored.
        }

        if (!contentLengthIsSet && method.equalsIgnoreCase(HttpRequest.POST)) {
            throw new HttpRequestFormatException("No content length for a POST request.\n");
        }

        String entityBody = null;
        /**
         * Get the entity body if any
         */
        if (contentLength > 0) {
            try {
                entityBody = getBody(in, contentLength);
            }
            catch (IOException e) {
                throw new HeaderIOException("Problem reading the entity body.\n");
            }
        }

         // Build the HttpRequestObject
        return new HttpRequest.Builder(method)
                .requestURI(requestURI)
                .httpVersion(httpVersion)
                .contentLength(contentLength)
                .entityBody(entityBody)
                .build();

    }

    /**
     * Reads and returns the header lines from a raw character stream, enforcing crlf line terminations
     */
    private ArrayList<String> getHeader(BufferedReader in) throws IOException {

        ArrayList<String> header = new ArrayList<>();
        boolean cr = false;
        boolean justReadCRLF = false;
        int fromServer;
        char currentChar;
        StringBuilder sb = new StringBuilder();

        //add the header lines, separated by CRLF
        while ((fromServer = in.read()) != -1) {

            currentChar = (char)fromServer;

            if (cr && currentChar == '\n') { // crlf
                header.add(sb.toString());
                sb = new StringBuilder();

                if (justReadCRLF) { //two crlf in a row = end of header lines
                    break;
                }
                cr = false;
                justReadCRLF = true;
            }
            else if (currentChar == '\r') {
                if (cr) {
                    justReadCRLF = false; // two cr's in a row breaks a possible crlfcrlf sequence
                }
                else cr = true;
            }
            else {
                cr = false;
                justReadCRLF = false;
                sb.append(currentChar);
            }
        }

        return header;
    }

    private String getBody(BufferedReader in, int size) throws  IOException{

        int currentChar;
        int byteCount = 0;
        StringBuilder sb = new StringBuilder();

        while (byteCount < size && (currentChar = in.read()) != -1) {

            // read() gives an int from 0 to 65535
            // We need to know whether the character is a 1-byte, or 2-byte character
            byteCount += (currentChar > 127)? 2 : 1;
            sb.append((char)currentChar);
        }

        return sb.toString();
    }



    /**
     * used in debug mode
     */
    void printRawRequestLines(ArrayList<String> header, ArrayList<String> body, PrintWriter pw) {
        for (String line : header) {
            pw.println(line);
        }
        pw.println();
        for (String line : body) {
            pw.println(line);
        }
        //pw.println();
    }

    void printRawHeader(ArrayList<String> header) {
        for (String line : header) {
            System.out.println(line);
        }

        System.out.println();
    }

}
