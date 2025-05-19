package ru.netology;

public enum FormEnctype {
    APPLICATION("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    TEXT("text/plane");

    private String enctype;
    FormEnctype(String enctype){
        this.enctype = enctype;
    }
    public String getEnctype(){
        return enctype;
    }
}
