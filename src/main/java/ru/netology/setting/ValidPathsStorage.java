package ru.netology.setting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ValidPathsStorage implements IValidPaths{
    private List<String> listPaths = new CopyOnWriteArrayList<> (List.of(
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
        private static ValidPathsStorage validPathsStorage = new ValidPathsStorage();
    }
    private ValidPathsStorage(){}

    public static ValidPathsStorage getInstance(){
        return ValidPathsStorageHead.validPathsStorage;
    }

    @Override
    public List<String> getValidPaths() {
        return listPaths;
    }
}
