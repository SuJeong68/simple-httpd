package com.nhnacademy.aiot;

public enum HttpStatus {
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict");


    HttpStatus(int status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    final int status;
    final String reason;

    @Override
    public String toString() {
        return status + " " + reason;
    }
}
