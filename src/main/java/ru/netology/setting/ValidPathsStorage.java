package ru.netology.setting;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ValidPathsStorage implements IValidPaths{
    private static final String PATH_DIRECTORY = "public";
    private final List<String> listPaths = new CopyOnWriteArrayList<> (List.of(
            "/index.html",
            "/spring.svg",
            "/spring.png",
            "/resources.html",
            "/styles.css",
            "/app.js",
            "/links.html",
            "/forms.html",
            "/classic.html",
            "/events.html",
            "/events.js"));
    private static class ValidPathsStorageHead{
        private static final ValidPathsStorage validPathsStorage = new ValidPathsStorage();
    }
    private ValidPathsStorage(){}

    public static ValidPathsStorage getInstance(){
        return ValidPathsStorageHead.validPathsStorage;
    }

    @Override
    public List<String> getValidPaths() {
        return listPaths;
    }

    @Override
    public Optional<Path> getPathFile(String path){
        if(listPaths.contains(path)){
            return Optional.of(Path.of(".", PATH_DIRECTORY, path));
        }
        return Optional.empty();
    }
}
