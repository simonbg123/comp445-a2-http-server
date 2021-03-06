package main;

import http.HttpRequest;
import http.HttpRequestHandler;
import http.HttpResponse;
import http.HttpServer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import static http.HttpServer.VERSION_1_0;

public class HttpFileServer implements HttpRequestHandler {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");
    private final static String DEFAULT_CONTENT_DISPOSITION = "inline";

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

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

    /* Threadsafe method to read and return the contents of a file
     * Acquires a read lock preventing another thread from modifying
     * a file while it's being read
     */
    private HttpResponse handleGET(HttpRequest httpRequest) {

        r.lock();

        try {
            HttpResponse httpResponse = null;
            String path = rootDir + httpRequest.getRequestURI();

            if (! pathIsWithinRootDir(path)) {
                String message = "Access forbidden for path: " + path + "\n";
                return HttpServer.getErrorResponse(HttpResponse.FORBIDDEN_403, message);
            }

            File file = new File(path);


            if (file.isDirectory()) {

                File[] fileList = file.listFiles();
                StringBuilder sb = new StringBuilder();
                if (fileList != null && fileList.length > 0){
                    for (File dirEntity : fileList) {
                        sb.append(dirEntity.getName());
                        sb.append(dirEntity.isDirectory() ? File.separator + "\n" : "\n");
                    }
                }
                else {
                    sb.append("Empty directory\n");
                }

                return getResponse(HttpResponse.OK_200, sb.toString());
            }
            else if (!file.isFile()){
                return HttpServer.getErrorResponse(HttpResponse.NOT_FOUND_404, "Resource does not exist.\n");
            }
            else if (!file.canRead()) {
                String message = "Cannot read the specified file: " + path + "\n";
                return HttpServer.getErrorResponse(HttpResponse.FORBIDDEN_403, message);
            }


            //if ok, get the string and make a 200ok response with body
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {

                StringBuilder sb = new StringBuilder();
                String line;
                for (line = br.readLine(); line != null; line = br.readLine()) {
                    sb.append(line + '\n');
                }

                httpResponse = getResponse(HttpResponse.OK_200, sb.toString());

                // get file extension
                Optional<String> extension = getExtension(path);
                if(extension.isPresent()) {
                    String ext = HttpResponse.getExtensionString(extension.get());
                    if (ext != null) {
                        httpResponse.setContentType(ext);
                    }
                }


            }
            catch (FileNotFoundException fnf) {
                String message = "Couldn't find resource: " + path + "\n";
                httpResponse = HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
            }
            catch (IOException e) {
                String message = "Problem reading the specified file: " + path + "\n";
                httpResponse = HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
            }

            return httpResponse;
        }
        finally {
            r.unlock();
        }

    }

    /* Creates a file and any needed directories requested by the client
     * and writes to the file any content specified in the request.
     * Uses a writer's lock to make sure no other thread is accessing the file
     * system while this method is executing.
     */
    private HttpResponse handlePOST(HttpRequest httpRequest) {

        w.lock();

        try {
            String fullPath = rootDir + httpRequest.getRequestURI();

            File file = new File(fullPath);

            if (! pathIsWithinRootDir(fullPath)) {
                String message = "Access forbidden for path: " + fullPath + "\n";
                return HttpServer.getErrorResponse(HttpResponse.FORBIDDEN_403, message);
            }

            String path = file.getParent();

            File folder = new File(path);

            if (!folder.isDirectory()) {

                if (!folder.mkdirs()) {
                    String message = "Couldn't make directory(ies): " + path + "\n";
                    return HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
                }
            }
            else if (!folder.canWrite()) {
                String message = "Can't write to directory: " + path + "\n";
                return HttpServer.getErrorResponse(HttpResponse.FORBIDDEN_403, message);
            }

            try {

                if (!file.exists()) {
                    boolean success = file.createNewFile();
                    if (!success) throw new IOException("Cannot create new file: " + path + "\n");
                }

            }
            catch (IOException e) {
                return HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, e.getMessage());
            }

            if (!file.canWrite()) {
                return HttpServer.getErrorResponse(HttpResponse.FORBIDDEN_403, "\nCan't write to file: " + file.getPath() + "\n");
            }

            //write content to file, or overwrite with new or no content
            try (PrintWriter pw = new PrintWriter(file)) {
                String content = httpRequest.getEntityBody();
                pw.print(content!= null ? content : "");
            }
            catch (FileNotFoundException fnf) {
                String message = "Problem writing to file: " + file.getPath();
                return HttpServer.getErrorResponse(HttpResponse.INTERNAL_SERVER_ERROR_500, message);
            }

            String message = "File '" + file.getPath() + "' created successfully. Size: " + httpRequest.getContentLength() + "\n";
            return getResponse(HttpResponse.CREATED_201, message);

        }
        finally {
            w.unlock();
        }
    }

    private HttpResponse getResponse(String statusAndReason, String message) {

        int contentLength = message.getBytes().length;

        return new HttpResponse.Builder(VERSION_1_0)
                .statusCodeAndReasonPhrase(statusAndReason)
                .date(formatter.format(ZonedDateTime.now()))
                .contentType(HttpResponse.contentTypePlainText)
                .contentLength(contentLength)
                .contentDisposition(DEFAULT_CONTENT_DISPOSITION)
                .entityBody(message)
                .build();
    }

    private HttpResponse getResponse(String statusAndReason) {

        return getResponse(statusAndReason, "");
    }

    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private boolean pathIsWithinRootDir(String path) {
        Path normalizedPath = Paths.get(path).normalize();
        String regex = "^" + Pattern.quote(rootDir) + "(" + Pattern.quote(File.separator) + ".*)?$";
        return normalizedPath.toString().matches(regex);

    }
}
