package test;

import http.HttpServer;

import java.io.IOException;
import java.sql.SQLOutput;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MainTest {
    public static void main(String[] args) {
        /*String s = "hello there hey";
        System.out.println(s.length());

        StringBuilder sb = new StringBuilder();

        int[] arr = {65, 75, 99, 101, 13, 10};
        for (int i : arr) {
            sb.append((char)i);
        }

        System.out.println(sb.toString() + " " + sb.toString().length());*/
        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");
        System.out.println(formatter.format(ZonedDateTime.now()));*/

        String line = "  Content-LEngth: 675";
        int contentLength = 0;
        if (line.toLowerCase().matches("^content-length: [0-9]+$")) {
            contentLength = Integer.parseInt(line.split("\\s")[1]);
        }
        System.out.println(contentLength);

        HttpServer server = new HttpServer(80, new MockHttpRequestHandler(), true);

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
