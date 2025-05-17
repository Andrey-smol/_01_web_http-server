package ru.netology;


import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ClientSocket implements Runnable {

    private final Socket clientSocket;
    private final Server server;
    private final BufferedReader in;
    private final BufferedOutputStream out;

    public ClientSocket(Socket socket, Server server) throws IOException {

        this.server = server;
        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            final var requestLines =  in.readLine();
            Request request = new Request();
            if(request.parsingRequestLine(requestLines)){
                Map<String, IHandler> map = server.getHandlersByKey(request.getMethod().get());
                map.get(request.getMessage()).handle(request, out);
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

}
