package com.nhnacademy.aiot;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private static final String VERSION = "HTTP/1.1";
    private HttpStatus status;
    private HashMap<String, String> headers;
    private String body;

    public Response() {
        headers = new HashMap<>();
    }

    public String getVersion() {
        return VERSION;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public boolean isExistsBody() {
        return body != null && !body.isBlank();
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addBody(String body) {
        this.body = body;
    }
}
