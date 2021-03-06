package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class HttpServer {

    /**
     * public constants
     */
    public static final String VERSION_1_0 = "HTTP/1.0";


    static final String CLIENT_SOCKET_PROBLEM = "Problem creating socket for client connection";
    private static final String WAITING_FOR_CONNECTION_PROBLEM = "Problem while waiting for client connection";
    private boolean verbose; // to comply with the assignment command line option
    private int portNumber;
    private HttpRequestHandler requestHandler;
    private final Object verboseOutputLock  = new Object();

    public HttpServer(int portNumber, HttpRequestHandler requestHandler) {
        this.portNumber = portNumber;
        this.requestHandler = requestHandler;
        verbose = false;
    }

    public HttpServer(int portNumber, HttpRequestHandler requestHandler, boolean verbose) {
        this.portNumber = portNumber;
        this.requestHandler = requestHandler;
        this.verbose = verbose;
    }

    public void run() throws IOException {

        ServerSocket serverSocket = new ServerSocket(portNumber);

        if (verbose) System.out.println("\nServer is running...\n");

        while(true) {

            try {

                new HttpServerThread(serverSocket.accept(), requestHandler, verbose, verboseOutputLock).start();

            }
            catch (IOException ioe) {
                if (verbose) {
                    System.out.println("\n" + WAITING_FOR_CONNECTION_PROBLEM + " :\n" + ioe.getMessage() + "\n");
                }
            }
            catch (Exception e) {
                if(verbose) {
                    System.out.println("\n" + CLIENT_SOCKET_PROBLEM + " :\n" + e.getMessage() + "\n");
                }
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
    static HttpRequest extractRequest(BufferedReader in) throws HeaderIOException, HttpRequestFormatException, HttpRequestUnsupportedVersionException {

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
            throw new HttpRequestUnsupportedVersionException("Unsupported version: " + httpVersion + "\n");
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
    private static ArrayList<String> getHeader(BufferedReader in) throws IOException {

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

    private static String getBody(BufferedReader in, int size) throws  IOException{

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
     * used in verbose mode
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
