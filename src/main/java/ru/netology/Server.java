package ru.netology;

import ru.netology.setting.CommonSetting;
import ru.netology.setting.HandlerShaper;
import ru.netology.setting.ValidPathsStorage;

import java.io.*;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Map<String, Map<String, IHandler>> handlers = new ConcurrentHashMap<>(16);
    private static Server server;
    public static void main(String[] args) {
        server = new Server();

        var handlerShaper = new HandlerShaper(ValidPathsStorage.getInstance());
        for(String path: ValidPathsStorage.getInstance().getValidPaths()){
            server.addHandler(RequestMethods.GET.get(), path, handlerShaper.getHandler(RequestMethods.GET));
            server.addHandler(RequestMethods.POST.get(), path, handlerShaper.getHandler(RequestMethods.POST));
        }
        server.listen(CommonSetting.SERVER_PORT);
    }
    public void listen(int port){
        PoolThread poolThread = PoolThread.getInstance();

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                poolThread.getPool().submit(new ClientSocket(serverSocket.accept(), server));
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

    private void addHandler(String method, String path, IHandler handler){
        if(handlers.containsKey(method)){
          handlers.get(method).put(path, handler);
        }else {
            Map<String, IHandler> map = new ConcurrentHashMap<>(16);
            map.put(path, handler);
            handlers.put(method, map);
        }
    }
    public Map<String, IHandler> getHandlersByKey(String key){
        return handlers.get(key);
    }
}
