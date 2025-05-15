package ru.netology.setting;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface IValidPaths {
    List<String> getValidPaths();

    Optional<Path> getPathFile(String path);
}
