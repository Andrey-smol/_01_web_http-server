package ru.netology.setting;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.*;

public class CommonFilesResources {
    private ExecutorService pool =  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private static class FileStorageHead{
        private static final CommonFilesResources fileStorage = new CommonFilesResources();
    }
    private CommonFilesResources(){}
    public static CommonFilesResources getInstance(){
        return FileStorageHead.fileStorage;
    }

    public synchronized OptionalLong getLengthFile(Path path) throws IOException {
        if(Files.exists(path)){
            return OptionalLong.of(Files.size(path));
        }
        return OptionalLong.empty();
    }
    public synchronized Optional<String> getTypeFile(Path path) throws IOException {
        if(Files.exists(path)) {
            return Optional.of(Files.probeContentType(path));
        }
        return Optional.empty();
    }

    public synchronized Future<String> getContentFile(Path path) throws IOException {
        return pool.submit(()->Files.readString(path));
    }

    public synchronized Future<?> copyFileIntoOutputStream(Path path, OutputStream out) throws IOException {
        return pool.submit(()->Files.copy(path, out));
    }
}
