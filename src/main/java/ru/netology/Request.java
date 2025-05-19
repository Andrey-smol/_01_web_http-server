package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Request {

    private static final String SEPARATOR_INTO_REQUEST_LINE = " ";
    private static final String SEPARATOR_STRING = "\r\n";
    private static final int NUMBER_ELEMENT_INTO_REQUEST_LINE = 3;
    private RequestMethods method;
    private Map<String, String> listHeaders = new HashMap<>();

    private String message;
    private List<NameValuePair> listQueryParam = new ArrayList<>();
    private String body;


    public RequestMethods getMethod() {
        return method;
    }

    public void setMethod(RequestMethods method) {
        this.method = method;
    }

    public Map<String, String> getListHeaders() {
        return listHeaders;
    }

    public void setListHeadLines(Map<String, String> listHeaders) {
        this.listHeaders = listHeaders;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Optional<List<String>> getQueryParam(String name) {
        if (listQueryParam == null || name == null || name.isEmpty()) {
            return Optional.empty();
        }
        List<String> list = listQueryParam.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .map(NameValuePair::getValue)
                .toList();
        if (list.size() > 0) {
            return Optional.of(list);
        }
        return Optional.empty();
    }

    public Optional<List<NameValuePair>> getQueryParams() {
        return listQueryParam == null ? Optional.empty() : Optional.of(listQueryParam);
    }


    private boolean parsingRequestLine(String line) throws URISyntaxException {
        var parts = line.split(SEPARATOR_INTO_REQUEST_LINE);
        if (parts.length != NUMBER_ELEMENT_INTO_REQUEST_LINE) {
            return false;
        }
        method = parts[0].transform(s -> {
            for (var t : RequestMethods.values()) {
                if (t.get().equals(s)) {
                    return t;
                }
            }
            return null;
        });
        if (method == null) {
            return false;
        }
        var str = parts[1].split("\\?");
        message = str[0];
        if (!message.startsWith("/")) {
            return false;
        }

        if (str.length > 1) {
            listQueryParam = URLEncodedUtils.parse(new URI(parts[1]), "UTF-8");
            listQueryParam.forEach(System.out::println);
        }

        System.out.println("***********************REQUEST_LINE********************");
        System.out.println(method + " " + message);
        return true;
    }

    private boolean parsingHeaders(byte[] buffer, int startNumberByte, int endNumberByte) {
        int len = endNumberByte - startNumberByte;
        byte[] buf = new byte[len];
        System.arraycopy(buffer, startNumberByte, buf, 0, endNumberByte - startNumberByte);

        List<String> list = Arrays.asList(new String(buf).split(SEPARATOR_STRING));

        for (String str : list) {
            int idx = str.indexOf(":");
            listHeaders.put(str.substring(0, idx).trim(), str.substring(idx + 1).trim());
        }
        System.out.println("***************HEADERS*****************");
        listHeaders.forEach((key, value) -> System.out.println(key + ":" + value));
        return true;
    }

    public boolean parseRequest(BufferedInputStream in) throws IOException, URISyntaxException {
        // лимит на request line + заголовки
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var length = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, length);
        if (requestLineEnd == -1) {
            return false;
        }
        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd));
        if (!parsingRequestLine(requestLine)) {
            return false;
        }
        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, length);
        if (headersEnd == -1) {
            return false;
        }
        if (!parsingHeaders(buffer, headersStart, headersEnd)) {
            return false;
        }

        // для GET тела нет
        if (method != RequestMethods.GET) {
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(listHeaders, "Content-Length");
            if (contentLength.isPresent()) {
                // отматываем на начало буфера
                in.reset();
                in.skip(headersEnd + headersDelimiter.length);
                final var len = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(len);
                body = new String(bodyBytes);
                System.out.println("****************BODY****************");
                System.out.println(body);
            }
        }
        return true;
    }

    // from google guava with modifications
    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private Optional<String> extractHeader(Map<String, String> headers, String header) {
        if (headers.containsKey(header)) {
            return Optional.of(headers.get(header));
        }
        return Optional.empty();
    }
}
