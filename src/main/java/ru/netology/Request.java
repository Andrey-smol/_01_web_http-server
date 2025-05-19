package ru.netology;

import org.apache.commons.fileupload2.core.*;
import org.apache.commons.fileupload2.javax.JavaxServletDiskFileUpload;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Request {

    private static final String SEPARATOR_INTO_REQUEST_LINE = " ";
    private static final String SEPARATOR_STRING = "\r\n";
    private static final int NUMBER_ELEMENT_INTO_REQUEST_LINE = 3;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private RequestMethods method;
    private Map<String, String> listHeaders = new HashMap<>();

    private String message;
    private List<NameValuePair> listQueryParam = new ArrayList<>();
    private List<NameValuePair> listBodyParam = new ArrayList<>();
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
        return getParam(name, listQueryParam);
    }

    public Optional<List<NameValuePair>> getQueryParams() {
        return listQueryParam == null ? Optional.empty() : Optional.of(listQueryParam);
    }

    public Optional<List<String>> getBodyParam(String name) {
        return getParam(name, listBodyParam);
    }

    private Optional<List<String>> getParam(String name, List<NameValuePair> list) {
        if (list == null || name == null || name.isEmpty()) {
            return Optional.empty();
        }
        List<String> l = list.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .map(NameValuePair::getValue)
                .toList();
        if (list.size() > 0) {
            return Optional.of(l);
        }
        return Optional.empty();
    }

    public Optional<List<NameValuePair>> getBodyParams() {
        return listBodyParam == null ? Optional.empty() : Optional.of(listBodyParam);
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

    private void parsingBodyParamsByMultipartInput(String contentType) throws IOException {
        ByteArrayInputStream content = new ByteArrayInputStream(body.getBytes());
        byte[] boundary = contentType.split("=")[1].trim().getBytes();
        MultipartInput multipartInput = new MultipartInput.Builder()
                .setInputStream(content)
                .setBoundary(boundary)
                .setBufferSize(512000000)
                .get();
        boolean nextPart = multipartInput.skipPreamble();
        while (nextPart) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String header = multipartInput.readHeaders().trim();

            if (!header.contains("filename")) {
                System.out.print("Headers: ");
                System.out.print(header);
                System.out.println();
                System.out.print("Body: ");
                multipartInput.readBodyData(out);
                System.out.println(out);
                listBodyParam.add(new NameValuePair() {
                    @Override
                    public String getName() {
                        return header;
                    }

                    @Override
                    public String getValue() {
                        String value = out.toString().trim();
                        if (value.charAt(0) == '=') {
                            return value.substring(1).trim();
                        }
                        return value;
                    }
                });
            } else {
                Optional<String[]> list = Arrays.stream(header.split(";")).toList()
                        .stream()
                        .filter(s -> s.contains("filename"))
                        .map(s -> s.split("="))
                        .findFirst();
                if (list.isPresent() && list.get().length >= 2) {
                    String nameFile = list.get()[1].trim();
                    int len = nameFile.length();
                    int idx = nameFile.indexOf('"');
                    if (idx != -1) {
                        nameFile = nameFile.substring(idx + 1);
                        idx = nameFile.indexOf('"');
                        if (idx != -1) {
                            nameFile = nameFile.substring(0, idx);
                        }
                    }
                    try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("files\\" + nameFile))) {
                        multipartInput.readBodyData(outputStream);
                        outputStream.flush();
                    }
                }
            }
            System.out.println();
            nextPart = multipartInput.readBoundary();
            out.close();
        }
    }

    private void parsingBodyParamsByFileItemFactory(String contentType) throws IOException {
        ByteArrayInputStream content = new ByteArrayInputStream(body.getBytes());

        DiskFileItemFactory factory = new DiskFileItemFactory.Builder().get();
        JavaxServletDiskFileUpload diskFileUpload = new JavaxServletDiskFileUpload(factory);
        diskFileUpload.setSizeMax(512000000);
        List<DiskFileItem> list = diskFileUpload.parseRequest(new RequestContext() {
            @Override
            public String getCharacterEncoding() {
                return StandardCharsets.UTF_8.name();
            }

            @Override
            public long getContentLength() {
                return body.length();
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public InputStream getInputStream() {
                return content;
            }
        });
        for (FileItem<DiskFileItem> item : list) {
            System.out.println(item);
            if (item.isFormField()) {
                listBodyParam.add(new NameValuePair() {
                    @Override
                    public String getName() {
                        return item.getFieldName();
                    }

                    @Override
                    public String getValue() {
                        return item.getString();
                    }
                });
            } else {
                String fieldName = item.getFieldName();
                String fileName = item.getName();
                String type = item.getContentType();
                boolean isInMemory = item.isInMemory();
                long sizeInBytes = item.getSize();
                System.out.println(fieldName + "; " + fileName + "; " + sizeInBytes);
                // Process a file upload
                Path uploadedFile = Paths.get(".", "files", fileName);
                item.write(uploadedFile);
            }
        }

    }

    private void parsingBodyParams() throws IOException {
        if (method == RequestMethods.POST) {
            Optional<String> op = extractHeader(listHeaders, CONTENT_TYPE);
            if (op.isPresent()) {
                String contentType = op.get();
                if (FormEnctype.APPLICATION.getEnctype().equalsIgnoreCase(contentType)) {
                    listBodyParam = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
                } else if (FormEnctype.TEXT.getEnctype().equalsIgnoreCase(contentType)) {
                    //to do
                } else {
                    if (contentType.contains(FormEnctype.MULTIPART.getEnctype())) {

                        //parsingBodyParamsByFileItemFactory(contentType);
                        parsingBodyParamsByMultipartInput(contentType);
                        System.out.println("*************************************************");
                    }
                }
            }
        }
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
            final var contentLength = extractHeader(listHeaders, CONTENT_LENGTH);
            if (contentLength.isPresent()) {
                // отматываем на начало буфера
                in.reset();
                in.skip(headersEnd + headersDelimiter.length);
                final var len = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(len);
                body = new String(bodyBytes);
                System.out.println("****************BODY****************");
                parsingBodyParams();
            }
        }
        getDebugReadParams();
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

    private void getDebugReadParams() {
        if (getQueryParams().isPresent()) {
            System.out.println("QueryParams");
            for (NameValuePair p : getQueryParams().get()) {
                System.out.println(p.getName() + " = " + p.getValue());
                System.out.println(getQueryParam(p.getName()).get());
            }
        }
        if (getBodyParams().isPresent()) {
            System.out.println("BodyParams");
            for (NameValuePair p : getBodyParams().get()) {
                System.out.println(p.getName() + " = " + p.getValue());
                System.out.println(getBodyParam(p.getName()).get());
            }
        }
    }
}
