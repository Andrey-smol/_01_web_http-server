package ru.netology;


import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ClientSocket implements Runnable {

    private final Socket clientSocket;
    private final Server server;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    public ClientSocket(Socket socket, Server server) throws IOException {

        this.server = server;
        this.clientSocket = socket;
        in = new BufferedInputStream(clientSocket.getInputStream());
        out = new BufferedOutputStream(clientSocket.getOutputStream());
    }
    @Override
    public void run() {
        try {
            Request request = new Request();
            if(request.parseRequest(in)){
                Map<String, IHandler> map = server.getHandlersByKey(request.getMethod().get());
                map.get(request.getMessage()).handle(request, out);
            }else {
                badRequest(out);
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
    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

}
