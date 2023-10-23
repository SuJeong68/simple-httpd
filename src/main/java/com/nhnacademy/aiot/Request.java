package com.nhnacademy.aiot;

import com.nhnacademy.aiot.exception.NotInPathFormatException;
import com.nhnacademy.aiot.exception.NotSupportedHttpMethodException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class Request {
    private static final String[] SUPPORTED_HTTP_METHODS = { "GET", "POST", "DELETE" };
    private static final String START_OF_PATH = "/";

    private String version;
    private String method;
    private String path;
    private HashMap<String, String> headers;
    private String body;

    public Request(String method, String path, String version) {
        if (!isSupportedHttpMethod(method)) {
            throw new NotSupportedHttpMethodException(method);
        }

        if (!isPath(path)) {
            throw new NotInPathFormatException(path);
        }

        this.method = method;
        this.path = path;
        this.version = version;
        headers = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public String getBody() {
        return body;
    }

    public boolean isExistHeader(String header) {
        return headers.containsKey(header);
    }

    private boolean isSupportedHttpMethod(String method) {
        return Arrays.stream(SUPPORTED_HTTP_METHODS).anyMatch(method::equalsIgnoreCase);
    }

    private boolean isPath(String path) {
        return path.startsWith(START_OF_PATH);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s %s %s", method, path, version)).append("\r\n");
        headers.forEach((key, value) -> sb.append(String.format("%s: %s", key, value)).append("\r\n"));

        return sb.toString();
    }
}
