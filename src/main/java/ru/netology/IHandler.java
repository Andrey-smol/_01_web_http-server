package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public interface IHandler {
    void handle(Request request, BufferedOutputStream out) throws IOException, ExecutionException, InterruptedException;
}
