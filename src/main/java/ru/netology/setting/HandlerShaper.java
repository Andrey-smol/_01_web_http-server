package ru.netology.setting;

import ru.netology.IHandler;
import ru.netology.RequestMethods;

import java.time.LocalDateTime;

public class HandlerShaper {
    private final IValidPaths validPaths;

    public HandlerShaper(IValidPaths validPaths) {
        this.validPaths = validPaths;
    }

    public IHandler getHandler(RequestMethods methods) {

        return (((request, out) -> {
            final var filePath = validPaths.getPathFile(request.getMessage()).get();
            final var filesResources = CommonFilesResources.getInstance();
            final var type = filesResources.getTypeFile(filePath);
            final var len = filesResources.getLengthFile(filePath);
            if (type.isPresent() && len.isPresent()) {
                final var mimeType = type.get();
                final var length = len.getAsLong();
                // special case for classic
                if (request.getMessage().equals("/classic.html")) {
                    final var template = filesResources.getContentFile(filePath).get();
                    final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                    out.write(getResponseHead(mimeType, content.length).getBytes());
                    out.write(content);
                } else {
                    out.write(getResponseHead(mimeType, length).getBytes());
                    filesResources.copyFileIntoOutputStream(filePath, out).get();
                }
                out.flush();
            } else {
                out.write(getResponseError404().getBytes());
                out.flush();
            }
        }));
    }

    private String getResponseHead(String mimeType, long lenBody) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + lenBody + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String getResponseError404() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

}
