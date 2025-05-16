package ru.netology;

public enum RequestMethods {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    PATCH("PATCH"),
    TRACE("TRACE"),
    CONNECT("CONNECT");
    private String method;
    RequestMethods(String method){
        this.method = method;
    }
    public String get(){
        return method;
    }
}
