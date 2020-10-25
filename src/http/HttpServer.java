package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HttpServer {

    private boolean debug; // to comply with the assignment command line option
    private int portNumber;
    ServerSocket serverSocket;
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

        serverSocket = new ServerSocket(portNumber);

        while(true) {
            
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out =
                         new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()))) {

                // get the request's header and body
                ArrayList<String> header = new ArrayList<>();
                ArrayList<String> body = new ArrayList<>();
                getHeaderAndBody(in, header, body);

                if (debug) printRawRequestLines(header, body);


            }


            //todo parse the input, build an http object
            // pass it to the handler get the object back
            // and print the answer as a string

        }
    }

    /*
     * Reads the request from the server and populates lists of
     * header lines and body lines.
     * Enforces proper line terminations crlf for the header.
     */
    private static void getHeaderAndBody(BufferedReader in, ArrayList<String> header, ArrayList<String> body) throws IOException {

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

        // add the body lines
        String currentLine;
        while ((currentLine = in.readLine()) != null) {
            body.add(currentLine);
        }
    }

    /**
     * used in debug mode
     */
    void printRawRequestLines(ArrayList<String> header, ArrayList<String> body) {
        for (String line : header) {
            System.out.println(line);
        }
        System.out.println();
        for (String line : body) {
            System.out.println(line);
        }
        System.out.println();
    }

    // parse request into an object
    // parse the request-line, parse its elements
    //todo not forget: crlf for end-of-line
}
