package test;

import http.HttpServer;
import main.HttpFileServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

        /*String line = "  Content-LEngth: 675";
        int contentLength = 0;
        if (line.toLowerCase().matches("^content-length: [0-9]+$")) {
            contentLength = Integer.parseInt(line.split("\\s")[1]);
        }
        System.out.println(contentLength);*/

        /*String path = "c:\\filedoesntexist.txt";
        File file = new File(path);

        if (!file.exists()) System.out.println("HEY");
        if (!file.isFile()) System.out.println("HO");
        if (!file.canRead()) System.out.println("TALBOT!");*/

        /*HttpServer server = new HttpServer(80, new MockHttpRequestHandler(), true);

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*String s = File.pathSeparator;

        String fullPath = "C:/Users/Simon\\doesntexist\\test.txt";
        String fullPath2 = "/hey.txt";

        File file = new File(fullPath);

        String path = file.getParent();
        String f = file.getName();

        System.out.println("'" + path + "'" + "\n" + f);*/

        //System.out.println(File.pathSeparator + " " + File.separator + " " + File.pathSeparatorChar + " " + File.separatorChar);


       /* try {

            String path = uri.getPath();
            System.out.println(path);

            *//*boolean res = new File(path).mkdirs();

            if (!res) System.out.println("didnt work");

            new File("C:\\Users\\Simon\\doesntexist\\test.txt").createNewFile();*//*

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }*/


        HttpServer server = new HttpServer(80, new HttpFileServer("C:\\Users\\Simon\\file_server"), true);

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
