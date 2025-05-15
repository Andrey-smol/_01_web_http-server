package ru.netology;

import ru.netology.setting.CommonFilesResources;
import ru.netology.setting.IValidPaths;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ClientSocket implements Runnable {

    private static final int NUMBER_ELEMENT_INTO_REQUEST_LINE = 3;
    private static final String SEPARATOR_INTO_REQUEST_LINE = " ";
    private final IValidPaths validPaths;
    private final Socket clientSocket;
    private final BufferedReader in;
    private final BufferedOutputStream out;
    private final CommonFilesResources filesResources;

    public ClientSocket(Socket socket, IValidPaths validPaths) throws IOException {

        this.clientSocket = socket;
        this.validPaths = validPaths;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedOutputStream(clientSocket.getOutputStream());
        filesResources = CommonFilesResources.getInstance();
    }

    @Override
    public void run() {
        try {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(SEPARATOR_INTO_REQUEST_LINE);

            if (parts.length == NUMBER_ELEMENT_INTO_REQUEST_LINE) {
                final var path = parts[1];
                final var pathValid = validPaths.getPathFile(path);
                if (pathValid.isPresent()) {
                    final var filePath = pathValid.get();
                    final var type = filesResources.getTypeFile(filePath);
                    final var len = filesResources.getLengthFile(filePath);
                    if (type.isPresent() && len.isPresent()) {
                        final var mimeType = type.get();
                        final var length = len.getAsLong();

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = filesResources.getContentFile(filePath).get();
                            final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                            out.write(getResponseHead(mimeType, content.length).getBytes());
                            out.write(content);
                        } else {
                            out.write(getResponseHead(mimeType, length).getBytes());
                            Object o = filesResources.copyFileIntoOutputStream(filePath, out).get();
                        }
                        out.flush();
                    } else {
                        out.write(getResponseError404().getBytes());
                        out.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            downService();
        }
    }

    private void downService() {
        try {
            if (!clientSocket.isClosed()) {
                in.close();
                out.close();
                clientSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
    private String getResponseHead(String mimeType, long lenBody){
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + lenBody + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
    private String getResponseError404(){
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
}
