package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Map<String, Map<String, IHandler>> handlers = new ConcurrentHashMap<>(16);

    public void listen(int port) {
        PoolThread poolThread = PoolThread.getInstance();

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                var socket = serverSocket.accept();
                poolThread.getPool().submit(new ClientSocket(socket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            poolThread.setInterrupt();
        }
    }

    public Map<String, Map<String, IHandler>> getAllHandlers() {
        return handlers;
    }

    public void addHandler(String method, String path, IHandler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            Map<String, IHandler> map = new ConcurrentHashMap<>(16);
            map.put(path, handler);
            handlers.put(method, map);
        }
    }

    public Map<String, IHandler> getHandlersByKey(String key) {
        return handlers.get(key);
    }
}
