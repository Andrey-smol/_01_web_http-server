package ru.netology;

import ru.netology.setting.CommonSetting;
import ru.netology.setting.ValidPathsStorage;

import java.io.*;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) {
        PoolThread poolThread = PoolThread.getInstance();

        try (final var serverSocket = new ServerSocket(CommonSetting.SERVER_PORT)) {
            while (true) {
                poolThread.getPool().submit(new ClientSocket(serverSocket.accept(), ValidPathsStorage.getInstance()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            poolThread.setInterrupt();
        }
    }
}
