package ru.netology;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Request {

    private static final String SEPARATOR_INTO_REQUEST_LINE = " ";
    private static final int NUMBER_ELEMENT_INTO_REQUEST_LINE = 3;
    private RequestMethods method;
    private List<String> listHeadLines = new ArrayList<>();

    private String message;
    private String body;

    

    public RequestMethods getMethod() {
        return method;
    }

    public void setMethod(RequestMethods method) {
        this.method = method;
    }

    public List<String> getListHeadLines() {
        return listHeadLines;
    }

    public void setListHeadLines(List<String> listHeadLines) {
        this.listHeadLines = listHeadLines;
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

    public boolean parsingRequestLine(String line){
        var parts = line.split(SEPARATOR_INTO_REQUEST_LINE);
        if (parts.length != NUMBER_ELEMENT_INTO_REQUEST_LINE) {
            return false;
        }
        method = parts[0].transform(s -> {
            for(var t: RequestMethods.values()){
                if(t.get().equals(s)){
                    return t;
                }
            }
            return null;
        });
        message = parts[1];
        return true;
    }
    public boolean parsingRequest(String request){
        return true;

    }
}
