package ru.netology;

import org.apache.commons.fileupload2.core.RequestContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestContextImpl implements RequestContext {
    private final byte[] body;
    private final String contentType;
    private final ByteArrayInputStream content;

    public RequestContextImpl(byte[] body, String contentType, ByteArrayInputStream content) {
        this.body = body;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public long getContentLength() {
        return body.length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() {
        return content;
    }
}
